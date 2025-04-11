// MySQL Setup

CREATE DATABASE bankingdb;

USE bankingdb;

CREATE TABLE account (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    balance DOUBLE
);

CREATE TABLE transaction_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT,
    receiver_id INT,
    amount DOUBLE,
    status VARCHAR(20)
);
// hibernate.cfg.xml (Hibernate Config)

<hibernate-configuration>
    <session-factory>
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/bankingdb</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">your_password</property>
        <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
        <property name="hibernate.hbm2ddl.auto">update</property>
        <property name="show_sql">true</property>

        <mapping class="Account"/>
        <mapping class="TransactionLog"/>
    </session-factory>
</hibernate-configuration>
// Account.java (Entity)

import javax.persistence.*;

@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private double balance;

    public Account() {}
    public Account(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getBalance() { return balance; }

    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return id + " | " + name + " | ₹" + balance;
    }
}
// TransactionLog.java (Entity)

import javax.persistence.*;

@Entity
@Table(name = "transaction_log")
public class TransactionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int senderId;
    private int receiverId;
    private double amount;
    private String status;

    public TransactionLog() {}
    public TransactionLog(int senderId, int receiverId, double amount, String status) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = status;
    }

    // Getters & setters
}
// AppConfig.java (Spring + Hibernate Setup)

import org.springframework.context.annotation.*;
import org.springframework.orm.hibernate5.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "your.package")
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:mysql://localhost:3306/bankingdb");
        ds.setUsername("root");
        ds.setPassword("your_password");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return ds;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
        sfb.setDataSource(dataSource());
        sfb.setPackagesToScan("your.package");
        sfb.getHibernateProperties().put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        sfb.getHibernateProperties().put("hibernate.show_sql", true);
        return sfb;
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager tx = new HibernateTransactionManager();
        tx.setSessionFactory(sessionFactory().getObject());
        return tx;
    }
}
// BankingService.java (Core Logic)

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.SessionFactory;
import org.hibernate.Session;

import javax.annotation.Resource;

@Service
public class BankingService {

    @Resource
    private SessionFactory sessionFactory;

    @Transactional
    public void transferMoney(int senderId, int receiverId, double amount) {
        Session session = sessionFactory.getCurrentSession();

        Account sender = session.get(Account.class, senderId);
        Account receiver = session.get(Account.class, receiverId);

        if (sender == null || receiver == null) {
            throw new RuntimeException("Invalid account(s)");
        }

        if (sender.getBalance() < amount) {
            session.save(new TransactionLog(senderId, receiverId, amount, "FAILED"));
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        session.update(sender);
        session.update(receiver);

        session.save(new TransactionLog(senderId, receiverId, amount, "SUCCESS"));
        System.out.println("Transfer complete: ₹" + amount);
    }
}
// MainApp.java (Demo)

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

        BankingService service = ctx.getBean(BankingService.class);

        // Demo accounts
        // You can also insert via SQL or manually
        // service.createAccounts(...)

        try {
            System.out.println("---- Success Transfer ----");
            service.transferMoney(1, 2, 1000);
        } catch (Exception e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }

        try {
            System.out.println("---- Failed Transfer (Insufficient Funds) ----");
            service.transferMoney(1, 2, 999999);
        } catch (Exception e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }

        ctx.close();
    }
}
