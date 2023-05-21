[
@[TOC](对象之间互相拷贝效率最高工具（mapstruct）)
# 目录结构介绍

![在这里插入图片描述](https://img-blog.csdnimg.cn/501e4f8cc83244d6a63a73ac91ca7ffc.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LyP5Yqg54m56YGH5LiK6KW_5p-a,size_20,color_FFFFFF,t_70,g_se,x_16)

# 一：pom依赖

```xml
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.4.2.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.4.2.Final</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version> 1.18.20</version>
        </dependency>
```

# 二：DO对象

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDo implements Serializable {
    private static final long serialVersionUID = 6110524897651597826L;

    private int id;

    private String name;

    private int age;

    private String address;

    private String role;

    private Date createDate;

    private Date updateDate;

    private String createUser;

    private  String updateUser;
}
```

# 三：Po对象

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPo implements Serializable {
    private static final long serialVersionUID = -3055967092435722132L;

    private int id;

    private String name;

    private int age;

    private String address;

    private String role;

    private Date createDate;

    private Date updateDate;

    private String createUser;

    private String updateUser;
}
```

# 四：转换工具

## BaseMapping

```java
package com.example.demo.utils;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.MapperConfig;

import java.util.List;
import java.util.stream.Stream;

@MapperConfig
public interface BaseMapping<SOURCE, TARGET> {

    /**
     * 映射同名属性
     */
    TARGET sourceToTarget(SOURCE var1);

    /**
     * 反向，映射同名属性
     */
    @InheritInverseConfiguration(name = "sourceToTarget")
    SOURCE targetToSource(TARGET var1);

    /**
     * 映射同名属性，集合形式
     */
    @InheritConfiguration(name = "sourceToTarget")
    List<TARGET> sourceToTarget(List<SOURCE> var1);

    /**
     * 反向，映射同名属性，集合形式
     */
    @InheritConfiguration(name = "targetToSource")
    List<SOURCE> targetToSource(List<TARGET> var1);

    /**
     * 映射同名属性，集合流形式
     */
    List<TARGET> sourceToTarget(Stream<SOURCE> stream);

    /**
     * 反向，映射同名属性，集合流形式
     */
    List<SOURCE> targetToSource(Stream<TARGET> stream);
}
```
## UserConverter
```java
package com.example.demo.converter;

import com.example.demo.entity.UserDo;
import com.example.demo.po.UserPo;
import com.example.demo.utils.BaseMapping;
import org.mapstruct.Mapper;

/**
 * @Description: do和po互相转换报告集合之前互相转换
 * @Date: 2021/9/23 14:49
 **/
@Mapper(componentModel = "spring")
public interface UserConverter extends BaseMapping<UserDo, UserPo> {
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/d055c6e607724c87b9eb88a4bcc9f6e8.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LyP5Yqg54m56YGH5LiK6KW_5p-a,size_20,color_FFFFFF,t_70,g_se,x_16)

# 五：测试

```java
 /**
     * 注入转换
     */
    @Autowired
    private UserConverter userConverter;
    /**
     * 实现Do映射Po，使用sourceToTarget
     */
    @Test
    void DoConverterPo() {
        UserDo userDo = new UserDo();
        userDo.setId(1);
        userDo.setName("小明");
        userDo.setAge(18);
        userDo.setRole("学生");
        userDo.setCreateDate(new Date());
        userDo.setCreateUser("李白");
        UserPo userPo = userConverter.sourceToTarget(userDo);
        System.out.println("userPo = " + userPo);
        //userPo = UserPo(id=1, name=小明, age=18, address=null, role=学生, createDate=Thu Sep 23 16:28:59 CST 2021, updateDate=null, createUser=李白, updateUser=null)
    }
    /**
     * 实现Po映射Do，使用targetToSource
     */
    @Test
    void PoConverterDo() {
        UserPo userPo = new UserPo();
        userPo.setId(1);
        userPo.setName("小明");
        userPo.setAge(18);
        userPo.setRole("学生");
        userPo.setCreateDate(new Date());
        userPo.setCreateUser("李白");
        UserDo userDo = userConverter.targetToSource(userPo);
        System.out.println("userDo = " + userDo);
        //userDo = UserDo(id=1, name=小明, age=18, address=null, role=学生, createDate=Thu Sep 23 16:31:25 CST 2021, updateDate=null, createUser=李白, updateUser=null)
    }
    /**
     * 实现Dos映射Pos，使用sourceToTarget
     */
    @Test
    void DoListConverterPoList() {
        List<UserDo> userDos = new ArrayList<>();
        UserDo userDo = new UserDo();
        userDo.setId(1);
        userDo.setName("小明");
        userDo.setAge(18);
        userDo.setRole("学生");
        userDo.setCreateDate(new Date());
        userDo.setCreateUser("李白");

        UserDo userDo2 = new UserDo();
        userDo2.setId(2);
        userDo2.setName("大明");
        userDo2.setAge(18);
        userDo2.setRole("学生");
        userDo2.setCreateDate(new Date());
        userDo2.setCreateUser("李白");

        userDos.add(userDo);
        userDos.add(userDo2);
        List<UserPo> userPos = userConverter.sourceToTarget(userDos);
        System.out.println("userPos = " + userPos);
        //userPos =
        // [UserPo(id=1, name=小明, age=18, address=null, role=学生, createDate=Thu Sep 23 16:41:27 CST 2021, updateDate=null, createUser=李白, updateUser=null),
        // UserPo(id=2, name=大明, age=18, address=null, role=学生, createDate=Thu Sep 23 16:41:27 CST 2021, updateDate=null, createUser=李白, updateUser=null)]
    }
```

# 六：当对象中有字段不一致的情况时，需要重写方法

```java
@Mapper(componentModel = "spring")
public interface UserConverter extends BaseMapping<UserDo, UserPo> {

    @Mapping(target = "fatherName", source = "userExetend.fatherName")
    @Mapping(target = "motherName", source = "userExetend.motherName")
    @Mapping(target = "xiongDi", source = "brother")
    @Override
    UserPo sourceToTarget(UserDo var1);
}
```
源码：
![在这里插入图片描述](https://img-blog.csdnimg.cn/9f5f71705df14c56a3bd074c43753ec2.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LyP5Yqg54m56YGH5LiK6KW_5p-a,size_20,color_FFFFFF,t_70,g_se,x_16)
注：在字段不一致手动映射时，打包失败，错误信息为：
`Error:(22,12) java: No property named "fatherName" exists in source parameter(s). Did you mean "null"?。
`[第一位博主也出现过类似问题](https://blog.csdn.net/qq_43686961/article/details/116376105)
[第二位博主的友情提示](https://javazhiyin.blog.csdn.net/article/details/109685127)
我也是有些奇怪，字段都是一样时使用没有任何问题，在这里踩了个坑。
mapstruct版本和lombok版本不一致导致。最后我将lombok版本降低为1.18.12问题就解决了。
此种搭配最终解决：
```xml
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.4.2.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.4.2.Final</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
        </dependency>
```

踩坑指南：
clean install之后在生成的target文件下查看具体实现
![在这里插入图片描述](https://img-blog.csdnimg.cn/5df13c29fb5b4937a36d4277aee6ecb3.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LyP5Yqg54m56YGH5LiK6KW_5p-a,size_20,color_FFFFFF,t_70,g_se,x_16)
如果出现映射丢失，或者没有映射主要排查如下情况：

1、lombok一定要放在mapstruct下面
![在这里插入图片描述](https://img-blog.csdnimg.cn/e7032c90cd274316840fec9a5fc9e924.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LyP5Yqg54m56YGH5LiK6KW_5p-a,size_20,color_FFFFFF,t_70,g_se,x_16)

2、检查lombok版本是否过低
测试时mapstuct版本为1.4.2.Final
lombok版本必须为1.16.16以上的高版本
![在这里插入图片描述](https://img-blog.csdnimg.cn/5565614f87bc442eb4a525286131d98b.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LyP5Yqg54m56YGH5LiK6KW_5p-a,size_20,color_FFFFFF,t_70,g_se,x_16)

```xml
     <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.4.2.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.4.2.Final</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.16</version>
        </dependency>
```










[1、干掉 BeanUtils！试试这款 Bean 自动映射工具，真心强大](https://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&mid=2247540947&idx=1&sn=a4941f93acb4ee551534a4a1f57d9376&chksm=eb50a5e5dc272cf3931a675c4b63cd3c2e4a4d7e72113e3fd18435033acb1fcb0b4b76a9dbf9&mpshare=1&scene=23&srcid=09064DXbvQ1399fpb00PXHaP&sharer_sharetime=1630910696265&sharer_shareid=bcf74325861a044e1ffc680c6d9a2afb#rd)
[2、mapstruct最佳实践 ](https://nullpointer.pw/mapstruct%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5.html)
[3、springboot整合mapstruct](https://blog.csdn.net/zfsn_zh/article/details/103807518)
[4、SpringBoot 2.4.1整合 MapStruct 详细教程](https://blog.csdn.net/cs1742121097/article/details/112671780)
[5、MapStruct文档（九）——高级映射选项](https://blog.csdn.net/sinat_32787481/article/details/110928756)
[6、MapStruct 1.3.0.Final参考指南](http://www.kailing.pub/MapStruct1.3/index.html)
]()
