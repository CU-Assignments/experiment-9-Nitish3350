//MySQL Table (run this first)

CREATE DATABASE studentdb;

USE studentdb;

CREATE TABLE student (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    age INT
);
// hibernate.cfg.xml (Hibernate Configuration)

<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">

<hibernate-configuration>
    <session-factory>
        <!-- DB connection -->
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/studentdb</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">your_password</property>

        <!-- Hibernate props -->
        <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
        <property name="hibernate.hbm2ddl.auto">update</property>
        <property name="show_sql">true</property>

        <!-- Mapped Entity -->
        <mapping class="Student"/>
    </session-factory>
</hibernate-configuration>
// Student.java (Entity Class)

import javax.persistence.*;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private int age;

    public Student() {}

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Getters & setters
    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    @Override
    public String toString() {
        return id + " | " + name + " | " + age;
    }
}
//StudentDAO.java (CRUD Logic)

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

public class StudentDAO {
    private static SessionFactory factory = new Configuration().configure().buildSessionFactory();

    public void addStudent(Student student) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(student);
        tx.commit();
        session.close();
        System.out.println("Student Added: " + student);
    }

    public Student getStudent(int id) {
        Session session = factory.openSession();
        Student student = session.get(Student.class, id);
        session.close();
        return student;
    }

    public void updateStudent(int id, String newName, int newAge) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        Student student = session.get(Student.class, id);
        if (student != null) {
            student.setName(newName);
            student.setAge(newAge);
            session.update(student);
        }
        tx.commit();
        session.close();
        System.out.println("Updated Student: " + student);
    }

    public void deleteStudent(int id) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        Student student = session.get(Student.class, id);
        if (student != null) {
            session.delete(student);
            System.out.println("Deleted Student: " + student);
        }
        tx.commit();
        session.close();
    }
}
//Main.java (Test the CRUD)

public class Main {
    public static void main(String[] args) {
        StudentDAO dao = new StudentDAO();

        // Create
        Student s1 = new Student("Nitish Goy", 21);
        dao.addStudent(s1);

        // Read
        Student s2 = dao.getStudent(s1.getId());
        System.out.println("Fetched: " + s2);

        // Update
        dao.updateStudent(s2.getId(), "Nitish the Goat", 22);

        // Delete
        dao.deleteStudent(s2.getId());
    }
}
