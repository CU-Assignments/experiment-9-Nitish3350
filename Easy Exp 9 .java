// 1. Course.java

public class Course {
    private String courseName;
    private int duration; // in months

    public Course(String courseName, int duration) {
        this.courseName = courseName;
        this.duration = duration;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return courseName + " (" + duration + " months)";
    }
}
// 2. Student.java

public class Student {
    private String name;
    private Course course;

    public Student(String name, Course course) {
        this.name = name;
        this.course = course;
    }

    public void displayInfo() {
        System.out.println("Student Name: " + name);
        System.out.println("Course Enrolled: " + course);
    }
}
// 3. AppConfig.java (DI Configuration)

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Course course() {
        return new Course("Full Stack Development", 6);
    }

    @Bean
    public Student student() {
        return new Student("Nitish Goy", course());
    }
}
//4. MainApp.java (Run It)

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        Student student = context.getBean(Student.class);
        student.displayInfo();
    }
}
