package java_web.repository;

import java_web.entity.Student;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

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
        try (Session session = sessionFactory.openSession()) {
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
        }
    }

    @Override
    public Student findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Query<Student> query = session.createQuery(
                    "FROM Student WHERE username = :username",
                    Student.class
            );
            query.setParameter("username", username);
            return query.getResultStream().findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    @Override
    public Student findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Student.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Student> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Student", Student.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Student> searchStudents(String keyword, int page, int pageSize, String sortField, boolean asc) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Student s WHERE (cast(s.id as string) LIKE :keyword OR s.name LIKE :keyword OR s.email LIKE :keyword)";
            hql += " ORDER BY s." + sortField + (asc ? " ASC" : " DESC");

            Query<Student> query = session.createQuery(hql, Student.class);
            query.setParameter("keyword", "%" + keyword + "%");
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }


    @Override
    public int countByKeyword(String keyword) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT COUNT(s.id) FROM Student s WHERE (cast(s.id as string) LIKE :keyword OR s.name LIKE :keyword OR s.email LIKE :keyword)";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("keyword", "%" + keyword + "%");
            Long count = query.uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }

    @Override
    public List<Student> sortStudents(String sortField, boolean asc, int page, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Student s ORDER BY s." + sortField + (asc ? " ASC" : " DESC");
            Query<Student> query = session.createQuery(hql, Student.class);
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }

    @Override
    public int countAll() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(s.id) FROM Student s", Long.class);
            Long count = query.uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }

    @Override
    public void toggleStatus(Integer id) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Student student = session.get(Student.class, id);
            if (student != null) {
                student.setStatus(!student.isStatus());
                session.update(student);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}