import entity.Item;
import jakarta.persistence.LockModeType;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Main {
    public static void main(String[] args) {
        final SessionFactory sessionFactory;

        try{
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }
        try(final ExecutorService executor = Executors.newFixedThreadPool(8)){
            final int totalTasks = 8;
            try{
                for (int i = 0; i < totalTasks; i++){

                    executor.submit(()-> {
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
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
