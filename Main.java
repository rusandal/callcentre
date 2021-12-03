import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    final static int serviceTime = 1000;
    final static int callsGeneratorCount = 60;
    final static int generatorSleep = 1000;
    static int generatorCount = 3;
    static ThreadLocal<Users> threadLocal = new ThreadLocal<>();
    static ConcurrentLinkedQueue<Users> callList = new ConcurrentLinkedQueue<>();


    public static void main(String[] args) {
        AtomicInteger count = new AtomicInteger(1);
        Thread thread = new Thread(() -> {
            while (generatorCount > 0) {
                for (int i = 1; i <= callsGeneratorCount; i++) {
                    callList.add(new Users());
                }
                //System.out.println("Сейчас в очереди " + callList.size() + " звонков");
                try {
                    Thread.sleep(generatorSleep);
                    generatorCount--;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Звонки перестали поступать. Сейчас в очереди " + callList.size() + " звонков");
        });
        thread.start();

        final ExecutorService threadPool = Executors.newFixedThreadPool(3);
        while (true) {
            threadPool.execute(() -> {
                threadLocal.set(callList.poll());
                if(threadLocal.get()!=null) {
                    System.out.println(count.getAndIncrement() + " " + Thread.currentThread().getName() + " взял звонок пользователя " + threadLocal.get());
                    try {
                        Thread.sleep(serviceTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (!thread.isAlive() & callList.isEmpty()) {
                break;
            }
        }
        threadPool.shutdown();
        System.out.println("Звонков в очереди: " + callList.size());
    }
}