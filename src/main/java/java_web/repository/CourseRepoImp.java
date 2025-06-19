package java_web.repository;

import java_web.entity.Course;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CourseRepoImp implements CourseRepo {
    private final SessionFactory sessionFactory;

    public CourseRepoImp(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Course> findByStatusWithPaging(boolean status, int page, int size) {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery("FROM Course WHERE status = :status", Course.class)
                    .setParameter("status", status)
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            session.close();
        }
    }

    @Override
    public Course getById(String id) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(Course.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(Course course) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(course);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean softDelete(String id) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Course course = session.get(Course.class, id);
            if (course != null) {
                course.setStatus(false);
                session.update(course);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean restore(String id) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Course course = session.get(Course.class, id);
            if (course != null) {
                course.setStatus(true);
                session.update(course);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public int countByStatus(boolean status) {
        Session session = sessionFactory.openSession();
        try {
            Long count = session.createQuery("SELECT COUNT(*) FROM Course WHERE status = :status", Long.class)
                    .setParameter("status", status)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Course> searchByName(String keyword, boolean status, int page, int size, String sortField, boolean asc) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "FROM Course WHERE status = :status AND name LIKE :kw ORDER BY " + sortField + (asc ? " ASC" : " DESC");
            return session.createQuery(hql, Course.class)
                    .setParameter("status", status)
                    .setParameter("kw", "%" + keyword + "%")
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .getResultList();
        } finally {
            session.close();
        }
    }

    @Override
    public int countByName(String keyword, boolean status) {
        Session session = sessionFactory.openSession();
        try {
            Long count = session.createQuery("SELECT COUNT(*) FROM Course WHERE status = :status AND name LIKE :kw", Long.class)
                    .setParameter("status", status)
                    .setParameter("kw", "%" + keyword + "%")
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Course> sortCourses(String sortField, boolean asc, boolean status, int page, int size) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "FROM Course WHERE status = :status ORDER BY " + sortField + (asc ? " ASC" : " DESC");
            return session.createQuery(hql, Course.class)
                    .setParameter("status", status)
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .getResultList();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Course> findAll() {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery("FROM Course where status = true ", Course.class).getResultList();
        } finally {
            session.close();
        }
    }

    @Override
    public int countByCourse(String courseId) {
        Session session = sessionFactory.openSession();
        try {
            Long count = session.createQuery("SELECT COUNT(*) FROM Enrollment WHERE course.id = :courseId", Long.class)
                    .setParameter("courseId", courseId)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean existsByCourseId(String courseId) {
        Session session = sessionFactory.openSession();
        try {
            Long count = session.createQuery(
                            "SELECT COUNT(c.id) FROM Course c WHERE c.id = :courseId", Long.class)
                    .setParameter("courseId", courseId)
                    .uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean existsByCourseName(String courseName) {
        Session session = sessionFactory.openSession();
        try {
            Long count = session.createQuery(
                            "SELECT COUNT(c.id) FROM Course c WHERE c.name = :courseName", Long.class)
                    .setParameter("courseName", courseName)
                    .uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }



}