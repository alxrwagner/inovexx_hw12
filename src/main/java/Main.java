import entity.Item;
import jakarta.persistence.LockModeType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Main {
    public static void main(String[] args) throws InterruptedException {
        final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        final ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Thread> threads = new ArrayList<>();
        try{
            final int totalTasks = 8;
            try{
                for (int i = 0; i < totalTasks; i++){

                    executor.submit(()-> {
                        Thread currentThread = Thread.currentThread();
                        threads.add(currentThread);
                        int count = 20000;
                        Random rand = new Random();
                        for(; count != 0; count--){
                            try(Session session = sessionFactory.openSession()){
                                session.beginTransaction();
                                Item item = session.find(Item.class, rand.nextInt(40) + 1, LockModeType.PESSIMISTIC_WRITE);
                                item.setVal(item.getVal() + 1);
                                session.merge(item);
                                session.getTransaction().commit();
                            }
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }finally {
            executor.shutdown();
        }

        for(Thread thread : threads){
            thread.join();
        }

        try(Session session = sessionFactory.openSession()){
            session.beginTransaction();
            int sumVal = 0;
            List<Item> items = session.createQuery("SELECT i FROM Item i;", Item.class).setLockMode(LockModeType.PESSIMISTIC_READ).getResultList();
            for (Item item : items){
                sumVal += item.getVal();
            }
            System.out.println(sumVal);
            session.getTransaction().commit();
        }
        HibernateUtil.shutdown();
    }
}
