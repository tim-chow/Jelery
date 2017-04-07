package cn.timd.Jelery;

import cn.timd.Jelery.Annotation.Task;
import cn.timd.Jelery.Exception.*;
import cn.timd.Jelery.TaskResult.AsyncTaskResult;
import cn.timd.Jelery.Utilities.ClassPathPackageScanner;
import cn.timd.Jelery.Vo.TaskMessageVo;
import cn.timd.Jelery.Vo.TaskResultVo;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractTaskBase implements TaskBase {
    private final static Map<String, AbstractTaskBase> taskRecords =
            new ConcurrentHashMap<String, AbstractTaskBase>();
    private final static Map<Class<?>, String> taskNames =
            new HashMap<Class<?>, String>();
    private static Configurable configurable;
    private static volatile boolean exit = false;

    public final String getTaskName() {
        return taskNames.get(getClass());
    }

    public static void register(AbstractTaskBase task, String taskName) {
        taskRecords.put(taskName, task);
        taskNames.put(task.getClass(), taskName);
    }

    public static void configure(Configurable configurable) {
        AbstractTaskBase.configurable = configurable;
    }

    private static synchronized void markAsExit() {
        exit = true;
    }

    public final AsyncTaskResult apply(String data)
            throws MessageBrokerException, UnsupportedEncodingException {
        String encoding = configurable.getEncoding();
        encoding = encoding == null ? "UTF-8": encoding;
        String taskID = UUID.randomUUID().toString();

        TaskMessageVo taskMessageVo = new TaskMessageVo();
        taskMessageVo.setData(data.getBytes(encoding));
        taskMessageVo.setTaskID(taskID);
        taskMessageVo.setTaskName(getTaskName());
        taskMessageVo.setEncoding(encoding);

        configurable.getMessageBroker().sendMessage(taskMessageVo,
                configurable.getMessageBrokerSendTimeoutMS());
        return new AsyncTaskResult(taskID, configurable.getResultBackend());
    }

    public static void taskScan(String... basePackages) {
        List<String> classNames = new ArrayList<String>();
        for (String basePackage: basePackages) {
            try {
                classNames.addAll(
                        new ClassPathPackageScanner(basePackage)
                                .getFullyQualifiedClassNameList()
                );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        for (String className: classNames) {
            try {
                Class<?> aClass = Class.forName(className);
                if (!aClass.isAnnotationPresent(Task.class) ||
                        !AbstractTaskBase.class.isAssignableFrom(aClass))
                    continue;

                for (Annotation annotation: aClass.getDeclaredAnnotations()) {
                    if (!annotation.annotationType().equals(Task.class))
                        continue;

                    String taskName = ((Task)annotation).name();
                    if (taskName.equals("AUTO"))
                        taskName = aClass.getName();

                    AbstractTaskBase task = (AbstractTaskBase) aClass.newInstance();
                    register(task, taskName);
                }
            } catch (ClassFormatError ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void realLogic() {
        TaskMessageVo taskMessage;
        AbstractTaskBase task;
        byte[] result;
        while (!exit && !Thread.currentThread().isInterrupted()) {
            TaskResultVo taskResult = new TaskResultVo();

            try {
                taskMessage = configurable.getMessageBroker().getMessage(
                        configurable.getMessageBrokerReadTimeoutMS());
                if (taskMessage == null)
                    break;
                task = taskRecords.get(taskMessage.getTaskName());
            } catch (MessageBrokerTimeoutException ex) {
                ex.printStackTrace();
                continue;
            } catch (MessageBrokerException ex) {
                ex.printStackTrace();
                continue;
            } catch (MessageFormatException ex) {
                ex.printStackTrace();
                continue;
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                continue;
            }

            try {
                if (configurable.getMaxRetryCount() != 0 &&
                        taskMessage.getRetryCount() >= configurable.getMaxRetryCount())
                    throw new MaxRetryCountReachedException(
                            "max retry count reached");
                result = task.run(taskMessage);
                taskResult.setState("SUCCESS");
                taskResult.setResult(result);
                taskResult.setEncoding(taskMessage.getEncoding());
            } catch (RetryException ex) {
                taskResult.setState("RETRY");
                taskResult.setExceptionClassName(ex.getClass().getName());
                taskResult.setExceptionMessage(ex.getMessage());
            } catch (Throwable ex) {
                taskResult.setState("FAIL");
                taskResult.setExceptionClassName(ex.getClass().getName());
                taskResult.setExceptionMessage(ex.getMessage());
            }

            if (taskResult.getState().equals("RETRY")) {
                try {
                    taskMessage.setRetryCount(taskMessage.getRetryCount() + 1);
                    configurable.getMessageBroker().sendMessage(taskMessage,
                            configurable.getMessageBrokerReadTimeoutMS());
                } catch (MessageBrokerTimeoutException ex) {
                    ex.printStackTrace();
                } catch (MessageBrokerException ex) {
                    ex.printStackTrace();
                }
            }

            try {
                configurable.getResultBackend().setResult(taskMessage.getTaskID(),
                        taskResult, configurable.getResultBackendSetTimeoutMS(),
                        configurable.getResultExpireTimeoutMS());
            } catch (ResultBackendTimeoutException ex) {
                ex.printStackTrace();
            } catch (ResultBackendException ex) {
                ex.printStackTrace();
            }

            try {
                Thread.sleep(configurable.getSleepTimeoutMS());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                break;
            }
        }
    }

    public static void run() {
        // handle signal
        Signal signal = new Signal("INT");
        Signal.handle(signal, new SignalHandler() {
            public void handle(Signal signal) {
                markAsExit();
            }
        });

        try {
            // // TODO: 2017/4/1 multi-threads supports here
            realLogic();
        } finally {
            configurable.close();
        }
    }
}
