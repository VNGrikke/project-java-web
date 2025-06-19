package java_web.repository;

import java_web.entity.Enrollment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EnrollmentRepoImp implements EnrollmentRepo {

    private final SessionFactory sessionFactory;

    public EnrollmentRepoImp(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Enrollment existsByStudentIdAndCourseId(Integer studentId, String courseId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Enrollment> query = session.createQuery(
                    "FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId",
                    Enrollment.class
            );
            query.setParameter("studentId", studentId);
            query.setParameter("courseId", courseId);
            return query.uniqueResult();
        }
    }

    @Override
    public List<String> findCourseIdsByStudentId(Integer studentId) {
        try (Session session = sessionFactory.openSession()) {
            Query<String> query = session.createQuery(
                    "SELECT e.course.id FROM Enrollment e WHERE e.student.id = :studentId",
                    String.class
            );
            query.setParameter("studentId", studentId);
            return query.getResultList();
        }
    }

    @Override
    public void add(Enrollment enrollment) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.save(enrollment);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            ex.printStackTrace();
        }
    }

    @Override
    public Enrollment getEnrollmentByIdAndStudentId(Integer id, Integer studentId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Enrollment> query = session.createQuery(
                    "FROM Enrollment WHERE id = :id AND student.id = :studentId",
                    Enrollment.class
            );
            query.setParameter("id", id);
            query.setParameter("studentId", studentId);
            return query.uniqueResult();
        }
    }

    @Override
    public void updateEnrollment(Enrollment e) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.update(e);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            ex.printStackTrace();
        }
    }

    @Override
    public List<Enrollment> findByStudentIdWithPaging(Integer studentId, int page, int size, String sortField, boolean asc) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Enrollment e WHERE e.student.id = :studentId ORDER BY e." + sortField + (asc ? " ASC" : " DESC");
            Query<Enrollment> query = session.createQuery(hql, Enrollment.class);
            query.setParameter("studentId", studentId);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Enrollment> searchByCourseNameAndStudentId(String keyword, Integer studentId, int page, int size, String sortField, boolean asc) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Enrollment e WHERE e.student.id = :studentId AND e.course.name LIKE :keyword ORDER BY e." + sortField + (asc ? " ASC" : " DESC");
            Query<Enrollment> query = session.createQuery(hql, Enrollment.class);
            query.setParameter("studentId", studentId);
            query.setParameter("keyword", "%" + keyword + "%");
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public int countByStudentId(Integer studentId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(e.id) FROM Enrollment e WHERE e.student.id = :studentId", Long.class)
                    .setParameter("studentId", studentId)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }

    @Override
    public int countByCourseNameAndStudentId(String keyword, Integer studentId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(e.id) FROM Enrollment e WHERE e.student.id = :studentId AND e.course.name LIKE :keyword", Long.class)
                    .setParameter("studentId", studentId)
                    .setParameter("keyword", "%" + keyword + "%")
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }

    @Override
    public int countAll() {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(*) FROM Enrollment", Long.class)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }


}