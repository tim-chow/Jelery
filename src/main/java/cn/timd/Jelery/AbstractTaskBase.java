package cn.timd.Jelery;

import cn.timd.Jelery.Annotation.Task;
import cn.timd.Jelery.Exception.MessageBrokerException;
import cn.timd.Jelery.Exception.MessageBrokerTimeoutException;
import cn.timd.Jelery.Exception.ResultBackendException;
import cn.timd.Jelery.Exception.ResultBackendTimeoutException;
import cn.timd.Jelery.TaskResult.AsyncTaskResult;
import cn.timd.Jelery.Utilities.ClassPathPackageScanner;
import cn.timd.Jelery.Vo.TaskMessageVo;
import cn.timd.Jelery.Vo.TaskResultVo;
import com.google.gson.JsonSyntaxException;

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

    public static synchronized void markAsExit() {
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
        while (!exit && !Thread.currentThread().isInterrupted()) {
            TaskMessageVo taskMessage;
            AbstractTaskBase task;
            byte[] result;
            TaskResultVo taskResult = new TaskResultVo();

            //// TODO: 2017/4/5 message ack and retry
            try {
                taskMessage = configurable.getMessageBroker().getMessage(
                        configurable.getMessageBrokerReadTimeoutMS());
                if (taskMessage == null)
                    break;
            } catch (MessageBrokerTimeoutException ex) {
                ex.printStackTrace();
                continue;
            } catch (MessageBrokerException ex) {
                ex.printStackTrace();
                continue;
            } catch (JsonSyntaxException ex) {
                ex.printStackTrace();
                continue;
            }

            try {
                task = taskRecords.get(taskMessage.getTaskName());
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                continue;
            }

            try {
                result = task.run(taskMessage);
                taskResult.setState("SUCCESS");
                taskResult.setResult(result);
                taskResult.setEncoding(taskMessage.getEncoding());
            } catch (Throwable ex) {
                taskResult.setState("FAIL");
                taskResult.setExceptionClassName(ex.getClass().getName());
                taskResult.setExceptionMessage(ex.getMessage());
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
        // // TODO: 2017/4/1 multi-threads supports here
        realLogic();
    }
}
