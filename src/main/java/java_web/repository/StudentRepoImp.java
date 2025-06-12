package java_web.repository;

import java_web.entity.Student;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public class StudentRepoImp implements StudentRepo {

    private final SessionFactory sessionFactory;

    public StudentRepoImp(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public boolean register(Student student) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.save(student);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public Student login(String username, String password) {
        Session session = sessionFactory.openSession();
        try {
            Query<Student> query = session.createQuery(
                    "FROM Student WHERE username = :username AND password = :password",
                    Student.class
            );
            query.setParameter("username", username);
            query.setParameter("password", password);
            return query.getResultStream().findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public Student findByUsername(String username) {
        Session session = sessionFactory.openSession();
        try {
            Query<Student> query = session.createQuery(
                    "FROM Student WHERE username = :username",
                    Student.class
            );
            query.setParameter("username", username);
            return query.getResultStream().findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(s.id) FROM Student s WHERE s.username = :username", Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(s.id) FROM Student s WHERE s.email = :email", Long.class)
                    .setParameter("email", email)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    @Override
    public boolean existsByPhone(String phone) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(s.id) FROM Student s WHERE s.phone = :phone", Long.class)
                    .setParameter("phone", phone)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

}