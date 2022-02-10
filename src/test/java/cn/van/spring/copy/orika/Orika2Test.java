package cn.van.spring.copy.orika;


import cn.van.spring.copy.orika.util.OrikaUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.util.Lists;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description OrikaUtils工具类的使用
 */
//@ExtendWith(SpringExtension.class)
@SpringBootTest
public class Orika2Test {

    /**
     * 基础实体映射
     * 只拷贝相同的属性
     */
    @Test
    public void convertObject() {
        Student student = new Student("1", "javadaily", "jianzh5@163.com");
        Teacher teacher = OrikaUtils.convert(student, Teacher.class);
        System.out.println(teacher);
    }

    /**
     * 实体映射 - 字段转换 拷贝不同属性
     * 此时由于对字段做了映射，可以将email映射到emailAddress
     * 注意这里的refMap中key放置的是源实体的属性，而value放置的是目标实体的属性，不要弄反了
     */
    @Test
    public void convertRefObject() {
        Student student = new Student("1", "javadaily", "jianzh5@163.com");

        Map<String, String> refMap = new HashMap<>(1);
        //map key 放置 源属性，value 放置 目标属性
        refMap.put("email", "emailAddress");
        Teacher teacher = OrikaUtils.convert(student, Teacher.class, refMap);
        System.out.println(teacher);
    }

    /**
     * 基础集合映射
     * 只拷贝相同的属性集合
     * 此时由于属性名不一致，集合中无法映射字段email
     */
    @Test
    public void convertList() {
        Student student1 = new Student("1", "javadaily", "jianzh5@163.com");
        Student student2 = new Student("2", "JAVA日知录", "jianzh5@xxx.com");
        List<Student> studentList = Lists.newArrayList(student1, student2);

        List<Teacher> teacherList = OrikaUtils.convertList(studentList, Teacher.class);

        System.out.println(teacherList);
    }

    /**
     * 集合映射 - 字段映射
     * 映射不同属性的集合
     */
    @Test
    public void convertRefList() {
        Student student1 = new Student("1", "javadaily", "jianzh5@163.com");
        Student student2 = new Student("2", "JAVA日知录", "jianzh5@xxx.com");
        List<Student> studentList = Lists.newArrayList(student1, student2);

        Map<String, String> refMap = new HashMap<>(2);
        //map key 放置 源属性，value 放置 目标属性
        refMap.put("email", "emailAddress");
        // 方法一
        List<Teacher> teacherList = OrikaUtils.convertList(studentList, Teacher.class, refMap);

        // 方法二
        //List<Teacher> teacherList = OrikaUtils.classMap(Student.class,Teacher.class,refMap).mapAsList(studentList,Teacher.class);

        System.out.println(teacherList);
    }

    /**
     * 数组和List的映射
     * <p>
     * 需要将Person类nameParts的值映射到Student中
     */
    @Test
    public void convertListObject() {
        Person person = new Person();
        person.setNameParts(Lists.newArrayList("1", "javadaily", "jianzh5@163.com"));

        Map<String, String> refMap = new HashMap<>(2);
        //map key 放置 源属性，value 放置 目标属性
        refMap.put("nameParts[0]", "id");
        refMap.put("nameParts[1]", "name");
        refMap.put("nameParts[2]", "email");

        Student student = OrikaUtils.convert(person, Student.class, refMap);
        System.out.println(student);
    }

    /**
     * 类类型映射
     * <p>
     * 需要将BasicPerson映射到Teacher
     */
    @Test
    public void convertClassObject() {
        BasicPerson basicPerson = new BasicPerson();
        Student student = new Student("1", "javadaily", "jianzh5@163.com");
        basicPerson.setStudent(student);

        Map<String, String> refMap = new HashMap<>(2);
        //map key 放置 源属性，value 放置 目标属性
        refMap.put("student.id", "id");
        refMap.put("student.name", "name");
        refMap.put("student.email", "emailAddress");

        Teacher teacher = OrikaUtils.convert(basicPerson, Teacher.class, refMap);
        System.out.println(teacher);
    }

    /**
     * 一对多映射
     * Student与Teacher的属性有email字段不相同，需要做转换映射；StudentGrade与TeacherGrade中的属性也需要映射
     * <p>
     * 多重映射到场景需要根据情况调用`OrikaUtils.register()`注册字段映射
     */
    @Test
    public void convertComplexObject() {
        Student student1 = new Student("1", "javadaily", "jianzh5@163.com");
        Student student2 = new Student("2", "JAVA日知录", "jianzh5@xxx.com");
        List<Student> studentList = Lists.newArrayList(student1, student2);

        StudentGrade studentGrade = new StudentGrade();
        studentGrade.setStudentGradeName("硕士");
        studentGrade.setStudentList(studentList);

        Map<String, String> refMap1 = new HashMap<>(1);
        //map key 放置 源属性，value 放置 目标属性
        refMap1.put("email", "emailAddress");
        OrikaUtils.register(Student.class, Teacher.class, refMap1);


        Map<String, String> refMap2 = new HashMap<>(2);
        //map key 放置 源属性，value 放置 目标属性
        refMap2.put("studentGradeName", "teacherGradeName");
        refMap2.put("studentList", "teacherList");


        TeacherGrade teacherGrade = OrikaUtils.convert(studentGrade, TeacherGrade.class, refMap2);
        System.out.println(teacherGrade);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Student {
    private String id;
    private String name;
    private String email;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Teacher {
    private String id;
    private String name;
    private String emailAddress;
}

@Data
class Person {
    private List<String> nameParts;
}

@Data
class BasicPerson {
    private Student student;
}

@Data
class StudentGrade {
    private String studentGradeName;
    private List<Student> studentList;
}

@Data
class TeacherGrade {
    private String teacherGradeName;
    private List<Teacher> teacherList;
}