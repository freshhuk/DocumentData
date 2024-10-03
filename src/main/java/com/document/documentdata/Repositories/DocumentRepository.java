package com.document.documentdata.Repositories;

import com.document.documentdata.Domain.Entities.Document;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {

    private final SessionFactory factory = new Configuration()
            .configure("hibernate.cfg.xml")
            .addAnnotatedClass(Document.class)
            .buildSessionFactory();

    public void add(Document doc){
        try(Session session = factory.openSession()){
            session.beginTransaction();
            session.persist(doc);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.out.println("Error with add in db");
        }
    }
    public boolean docIsExist(String docName){
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            Query<Document> query = session.createQuery("from Document where name = :docName", Document.class);
            query.setParameter("docName", docName);
            Document document = query.uniqueResult();
            session.getTransaction().commit();

            return document == null;
        } catch (Exception ex) {
            System.out.println("Error method existsByUsername" + ex);
            return false;
        }
    }

}
