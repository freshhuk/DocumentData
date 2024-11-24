package com.document.documentdata.Repositories;

import com.document.documentdata.Domain.Entities.Document;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {

    private static final Logger logger = LoggerFactory.getLogger(DocumentRepository.class);

    private final SessionFactory factory = new Configuration()
            .configure("hibernate.cfg.xml")
            .addAnnotatedClass(Document.class)
            .buildSessionFactory();


    public Document getByName(String name){
        try(Session session = factory.openSession()){

            session.beginTransaction();

            Query<Document> query = session.createQuery("from Document where name =:  name", Document.class);
            query.setParameter("name", name);

            Document document = query.uniqueResult();
            session.getTransaction().commit();

            return document;

        } catch (Exception ex) {
            logger.error("Error with getByName in db: " + ex);
            return null;
        }
    }

    public void deleteById(int id){
        try(Session session = factory.openSession()){
            session.beginTransaction();

            var model = session.get(Document.class, id);

            session.remove(model);
            session.getTransaction().commit();
        }
    }
    public void add(Document doc){
        try(Session session = factory.openSession()){
            session.beginTransaction();
            session.persist(doc);
            session.getTransaction().commit();
        } catch (Exception ex) {
            logger.error("Error with add in db: " + ex);
        }
    }

    public void update(Document doc){
        try(Session session = factory.openSession()){
            session.beginTransaction();

            var document = session.get(Document.class, doc.getId());

            document.setIdUserModify(doc.getIdUserModify());
            document.setModifyDate(doc.getModifyDate());

            session.merge(document); // we use marge for update own entity
            session.getTransaction().commit();
        }catch( Exception ex){
            logger.error("Error with update method: " + ex);
        }
    }

    public void deleteAll(){
        try(Session session = factory.openSession()){

            session.beginTransaction();

            session.createQuery("delete from Document").executeUpdate();

            session.getTransaction().commit();
        } catch (Exception ex){
            logger.error("Error in deleteAll: " + ex);
        }
    }


    public boolean docIsExist(String docName){
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            Query<Document> query = session.createQuery("from Document where name = :docName", Document.class);
            query.setParameter("docName", docName);
            Document document = query.uniqueResult();
            session.getTransaction().commit();

            return document != null;
        } catch (Exception ex) {
            logger.error("Error method existsByUsername" + ex);
            return false;
        }
    }

}
