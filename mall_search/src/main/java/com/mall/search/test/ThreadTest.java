package com.mall.search.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {
    static ExecutorService executor = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //任务01
        CompletableFuture<String> task01 = CompletableFuture.supplyAsync(() -> "task01", executor);
        //任务02
        CompletableFuture<String> task02 = CompletableFuture.supplyAsync(() -> "task02", executor);
//        //任务01合并任务02，两个任务执行结束后再执行指定任务
//        CompletableFuture<String> task03 = task01.thenCombine(task02, (res1, res2) -> {
//            System.out.println("01返回" + res1);
//            System.out.println("02返回" + res2);
//            return "task03";
//        });
//        System.out.println("03返回" + task03.get());

//        task01.runAfterBoth(task02, ()->{
//            System.out.println("前两个任务执行完了");
//        });

//        task01.thenAcceptBoth(task02, (res1, res2) -> {
//            System.out.println("01返回" + res1);
//            System.out.println("02返回" + res2);
//        });

        /*
            合并两个任务，只要有一个任务完成了就会执行新的任务
            task02：合并的任务
            () -> {}：Runnable对象，新建的任务
         */
//        task01.runAfterEither(task02, () -> {
//            System.out.println("有一个任务执行完了");
//        });
        /*
            合并两个任务，只要有一个任务完成了就会执行新的任务
            task02：合并的任务
            (res) -> {}：res是完成了的任务结果
         */
//        task01.acceptEither(task02, (res) -> {
//            System.out.println("执行完的任务结果是：" + res);
//        });
        CompletableFuture<String> task03 = task01.applyToEither(task02, (res) -> {
            System.out.println("执行完的任务结果是：" + res);
            return "task03";
        });
        System.out.println("task03的执行结果是：" + task03.get());
    }


    public void test01() {
        CompletableFuture.
                //Runnable接口的run()方法
                        runAsync(() -> System.out.println("task"), executor).
                //run()方法执行结束后执行的方法，res为返回结果，t为Throwable对象
                        whenComplete((res, t) -> {
                }).
                //出现异常时执行的方法
                        exceptionally(t -> null).
                //可以在这个方法中处理任务执行的结果
                        handle((res, e) -> null).
                /*
                    执行新的任务，消费上个任务的结果。
                        不带Async是串行，与上一个任务共用线程；
                        带Async会新建一个县城执行。
                    res：上个任务执行的结果
                 */
                        thenApply(res -> null).
                /*
                    与thenApply()方法作用相同，但没有返回结果
                 */
                        thenAccept(res -> {
                }).
                /*
                    上个任务完成后执行的任务，不消费上个任务的结果。
                    参数为Runnable对象
                 */
                        thenRun(() -> {
                });


        CompletableFuture.
                //Runnable接口的run()方法
                        supplyAsync(() -> {
                    System.out.println("task");
                    System.out.println(Thread.currentThread().getId());
                    return "task";
                }, executor).
                //run()方法执行结束后执行的方法，res为返回结果，t为Throwable对象
                        whenComplete((res, t) -> {
                    System.out.println("执行完成");
                }).
                //出现异常时执行的方法
                        exceptionally(t -> {
                    System.out.println("Throwable:" + t);
                    return "error";
                }).
                //可以在这个方法中处理任务执行的结果
                        handle((res, t) -> {
                    System.out.println("处理结果：" + res);
                    System.out.println("异常为：" + t);
                    System.out.println(Thread.currentThread().getId());
                    return res;
                }).
                /*
                    执行新的任务，消费上个任务的结果。
                        不带Async是串行，与上一个任务共用线程；
                        带Async会新建一个县城执行。
                    res：上个任务执行的结果
                 */
                        thenApply(res -> {
                    System.out.println(Thread.currentThread().getId() + "\t" + res);
                    return "thenApply";
                }).
                /*
                    与thenApply()方法作用相同，但没有返回结果
                 */
                        thenAcceptAsync(res -> {
                    System.out.println(Thread.currentThread().getId() + "\t" + res);
                }, executor).
                /*
                    上个任务完成后执行的任务，不消费上个任务的结果。
                    参数为Runnable对象
                 */
                        thenRun(() -> {
                    System.out.println("thenRun");
                });
    }
}
