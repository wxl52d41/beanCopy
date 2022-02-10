当==get/set==太繁琐时；当==BeanUtils==无法拷贝集合时；当。。。可能，你需要好好看看这篇文章，文末附完整示例代码。

在做业务的时候，为了隔离变化，我们会将==DAO==查询出来的==DO==和对前端提供的==DTO==隔离开来。大概90%的时候，它们的结构都是类似的；但是我们很不喜欢写很多冗长的==b.setF1(a.getF1())==这样的代码，于是我们需要简化对象拷贝方式。

# 一、背景

## 1.1 对象拷贝概念

Java中，数据类型分为值类型（基本数据类型）和引用类型，值类型包括==int==、==double==、==byte==、==boolean==、==char==等简单数据类型，引用类型包括类、接口、数组等复杂类型。

对象拷贝分为**浅拷贝(浅克隆)**与**深拷贝(深克隆)**。

**浅拷贝与深拷贝差异**
![浅拷贝与深拷贝差异](https://img-blog.csdnimg.cn/5185e43254064f0ca129c1812369c416.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5LyP5Yqg54m56YGH5LiK6KW_5p-a,size_20,color_FFFFFF,t_70,g_se,x_16)
==参考文章==：[Java的深拷贝和浅拷贝](https://www.cnblogs.com/ysocean/p/8482979.html)

## 1.2 为什么需要拷贝对象

1. ==Entity==对应的是持久层数据结构（一般是数据库表的映射模型）;
2. ==Model== 对应的是业务层的数据结构;
3. ==VO== 就是==Controller==和客户端交互的数据结构。

例如：数据库查询出来的用户信息(表的映射模型)是==UserDO==，但是我们需要传递给客户端的是==UserVO==,这时候就需要把==UserDO==实例的属性一个一个赋值到==UserVO==实例中。

**在这些数据结构之间很大一部分属性都可能会相同，也可能不同。**

## 1.3 有哪些拷贝方式

 1. ==org.springframework.beans.BeanUtils== ；
 2. ==org.springframework.cglib.beans.BeanCopier==；
 3. ==ma.glasnost.orika ；==
 4. ==org.mapstruct（强烈推荐）==。

# 二、BeanUtils

==Spring==中的==BeanUtils==，其中实现的方式很简单，就是对两个对象中相同名字的属性进行简单==get/set==，仅检查属性的可访问性。

[BeanUtils 源码](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/BeanUtils.html)

可以看到, 成员变量赋值是基于目标对象的成员列表, 并且会跳过==ignore==的以及在源对象中不存在的, 所以这个方法是安全的, 不会因为两个对象之间的结构差异导致错误, 但是必须保证同名的两个成员变量类型相同。
UserDO 
```java
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserDO {

    private Long userId;

    private String userName;

    private Integer age;

    private Integer sex;
}

```
UserVO 
```java
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserVO {

    private Long userId;

    private String userName;

    private Integer age;

    private String sex;
}
```

## 2.1、单个对象拷贝

我们把数据库查询出来的UserDO.java 拷贝到 UserVO.java。直接使用BeanUtils.copyProperties()方法。

```java
    /**
     * 单个对象拷贝
     * @author xlwang55
     * @date 2022/1/27
     */
    @Test
    public void commonCopy() {
        UserDO userDO = new UserDO(1L, "Van", 18, 1);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        System.out.println("userVO = " + userVO);
        //结果: userVO = UserVO(userId=1, userName=Van, age=18, sex=null)
    }
```

## 2.2、集合拷贝

刚刚拷贝的是一个对象，但是有时候我们想拷贝一组UerDO.java，是一个集合的时候就不能这样直接赋值了。如果还按照这种逻辑，如下：
```java
    /**
     * 集合拷贝
     * @author xlwang55
     * @date 2022/1/27
     */
    @Test
    public void listCopyFalse() {
        List<UserDO> userDOList = new ArrayList();
        userDOList.add(new UserDO(1L, "Van", 18, 1));
        userDOList.add(new UserDO(2L, "VanVan", 18, 2));
        List<UserVO> userVOList = new ArrayList();
        BeanUtils.copyProperties(userDOList, userVOList);
        System.out.println("userVOList = " + userVOList);
        //结果: userVOList = []
    }
```
通过日志可以发现，直接拷贝集合是无效的，那么怎么解决呢？

## 2.3 暴力拷贝（不推荐）

将需要拷贝的集合遍历，暴力拷贝。

```java
    /**
     * 暴力拷贝（不推荐） 将需要拷贝的集合遍历，暴力拷贝
     * 虽然该方式可以解决，但是一点都不优雅，特别是写起来麻烦
     * @author xlwang55
     * @date 2022/1/27
     */
    @Test
    public void listCopyCommon() {
        List<UserDO> userDOList = new ArrayList();
        userDOList.add(new UserDO(1L, "Van", 18, 1));
        userDOList.add(new UserDO(2L, "VanVan", 20, 2));
        List<UserVO> userVOList = new ArrayList();
        userDOList.forEach(userDO -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userDO, userVO);
            userVOList.add(userVO);
        });
        System.out.println("userVOList = " + userVOList);
        //结果:  userVOList = [UserVO(userId=1, userName=Van, age=18, sex=null), UserVO(userId=2, userName=VanVan, age=20, sex=null)]
    }
```
虽然该方式可以解决，但是一点都不优雅，特别是写起来麻烦。

## 2.4 优雅拷贝（本文推荐）

通过==JDK 8== 的函数式接口封装==org.springframework.beans.BeanUtils==

### 2.4.1 定义一个函数式接口
函数式接口里是可以包含默认方法，这里我们定义默认回调方法。

```java
@FunctionalInterface
public interface BeanUtilCopyCallBack <S, T> {
    /**
     * 定义默认回调方法
     * @param t
     * @param s
     */
    void callBack(S t, T s);
}
```
### 2.4.2 封装一个工具类== BeanUtilCopy.java==

```java
public class BeanUtilCopy extends BeanUtils {
    /**
     * 集合数据的拷贝
     *
     * @param sources: 数据源类
     * @param target:  目标类::new(eg: UserVO::new)
     * @return
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target) {
        return copyListProperties(sources, target, null);
    }

    /**
     * 带回调函数的集合数据的拷贝（可自定义字段拷贝规则）
     *
     * @param sources:  数据源类
     * @param target:   目标类::new(eg: UserVO::new)
     * @param callBack: 回调函数
     * @return
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target,
                                                    BeanUtilCopyCallBack<S, T> callBack) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = target.get();
            copyProperties(source, t);
            list.add(t);
            if (callBack != null) {
                // 回调
                callBack.callBack(source, t);
            }
        }
        return list;
    }
}
```
### 2.4.3 简单拷贝测试

```java
    @Test
    public void listCopyUp() {
        List<UserDO> userDOList = new ArrayList();
        userDOList.add(new UserDO(1L, "Van", 18, 1));
        userDOList.add(new UserDO(2L, "VanVan", 20, 2));
        List<UserVO> userVOList = BeanUtilCopy.copyListProperties(userDOList, UserVO::new);
        System.out.println("userVOList = " + userVOList);
        //结果: userVOList = [UserVO(userId=1, userName=Van, age=18, sex=null), UserVO(userId=2, userName=VanVan, age=20, sex=null)]
    }
```
通过如上方法，我们基本实现了集合的拷贝，但是从返回结果我们可以发现：属性不同的字段无法拷贝。

注意：
==UserDO.java== 和==UserVO.java== 最后一个字段==sex==类型不一样，分别是：==Integer/String==

**优化一下**

 - 新增性别枚举类


```java
public enum SexEnum {
    UNKNOW("未设置", 0),
    MEN("男生", 1),
    WOMAN("女生", 2);

    private String desc;
    
    private int code;

    SexEnum(String desc, int code) {
        this.desc = desc;
        this.code = code;
    }

    public static SexEnum getDescByCode(int code) {
        SexEnum[] typeEnums = values();
        for (SexEnum value : typeEnums) {
            if (code == value.getCode()) {
                return value;
            }
        }
        return null;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
```

 - 带特定转换的集合拷贝

```java
    @Test
    public void listCopyUpWithCallback() {
        List<UserDO> userDOList = new ArrayList();
        userDOList.add(new UserDO(1L, "Van", 18, 1));
        userDOList.add(new UserDO(2L, "VanVan", 20, 2));
        List<UserVO> userVOList = BeanUtilCopy.copyListProperties(userDOList, UserVO::new, (userDO, userVO) -> {
            // 这里可以定义特定的转换规则
            userVO.setSex(SexEnum.getDescByCode(userDO.getSex()).getDesc());
        });
        System.out.println("userVOList = " + userVOList);
        //结果: userVOList = [UserVO(userId=1, userName=Van, age=18, sex=男生), UserVO(userId=2, userName=VanVan, age=20, sex=女生)]
    }
```
通过打印结果可以发现，`UserDO.java` 中`Integer`类型的sex复制到`UserVO.java`成了`String`类型的男生/女生。

## 2.5 小结
该方法是我们用的最多的方案，这里简单封装下，可以方便集合类型对象的拷贝，平常使用基本够用，仅供参考。

# 三、BeanCopier
UserDO 
```java
@Data
public class UserDO {

    private int id;
    private String userName;
    private int sex;
    /**
     * 以下两个字段用户模拟自定义转换
     */
    private LocalDateTime gmtBroth;
    private BigDecimal balance;

    public UserDO(Integer id, String userName, int sex, LocalDateTime gmtBroth, BigDecimal balance) {
        this.id = id;
        this.userName = userName;
        this.sex = sex;
        this.gmtBroth = gmtBroth;
        this.balance = balance;
    }
}
```
UserDomain 
```java
@Data
public class UserDomain {
    private Integer id;
    private String userName;

    /**
     * 以下两个字段用户模拟自定义转换
     */
    private String gmtBroth;
    private String balance;
}
```

`BeanCopier`是用于在两个`bean`之间进行属性拷贝的。`BeanCopier`支持两种方式:

一种是不使用`Converter`的方式，仅对两个`bean`间属性名和类型完全相同的变量进行拷贝;
另一种则引入`Converter`，可以对某些特定属性值进行特殊操作。

## 3.1 常规使用
```java
    @Test
    public void normalCopy() {
        // 模拟查询出数据
        UserDO userDO = new UserDO(1L, "Van", 18, 1);
        System.out.println("拷贝前：userDO:{}" + userDO);

        // 第一个参数：源对象， 第二个参数：目标对象，第三个参数：是否使用自定义转换器（下面会介绍），下同
        BeanCopier b = BeanCopier.create(UserDO.class, UserVO.class, false);
        UserVO userVO = new UserVO();
        b.copy(userDO, userVO, null);
        System.out.println("拷贝后：userDTO:{}" + userVO);
        //拷贝前：userDO:{}UserDO(userId=1, userName=Van, age=18, sex=1)
        //拷贝后：userDTO:{}UserVO(userId=1, userName=Van, age=18, sex=null)
    }
```
通过结果发现：`UserDO`的`int`类型的`sex`无法拷贝到`UserDTO`的`Integer`的`sex`。

即：`BeanCopier`**只拷贝名称和类型都相同的属性。**

即使源类型是原始类型(`int`, `short`和`char`等)，目标类型是其包装类型(`Integer`, `Short`和`Character`等)，或反之：都不会被拷贝。

## 3.2 自定义转换器

通过`3.1`可知，当源和目标类的属性类型不同时，不能拷贝该属性，此时我们可以通过实现`Converter`接口来自定义转换器

 - 目标对象属性类

```java
@Data
public class UserDomain {

    private Integer id;
    private String userName;

    /**
     * 以下两个字段用户模拟自定义转换
     */
    private String gmtBroth;
    private String balances;
}
```

 - 实现Converter接口来自定义属性转换

```java
public  class UserDomainConverter implements Converter {

    /**
     * 时间转换的格式
     */
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 自定义属性转换
     * @param value 源对象属性类
     * @param target 目标对象里属性对应set方法名,eg.setId
     * @param context 目标对象属性类
     * @return
     */
    @Override
    public Object convert(Object value, Class target, Object context) {
        if (value instanceof Integer) {
            return value;
        } else if (value instanceof LocalDateTime) {
            LocalDateTime date = (LocalDateTime) value;
            return dtf.format(date);
        } else if (value instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) value;
            return bd.toPlainString();
        }
        // 更多类型转换请自定义
        return value;
    }
}

```

 - 测试方法


```java
/**
 * 类型不同,使用Converter
 */
@Test
    /**
     * 类型不同,使用Converter
     */
    @Test
    public void converterTest() {
        // 模拟查询出数据
        UserDO userDO = DataUtil.createData();
        log.info("拷贝前：userDO:{}", userDO);
        BeanCopier copier = BeanCopier.create(UserDO.class, UserDomain.class, true);
        UserDomainConverter converter = new UserDomainConverter();
        UserDomain userDomain = new UserDomain();
        copier.copy(userDO, userDomain, converter);
        log.info("拷贝后：userDomain:{}", userDomain);
        //拷贝前：userDO:UserDO(id=1, userName=Van, sex=0, gmtBroth=2022-02-10T10:32:25.803, balance=100)
        //拷贝后：userDomain:UserDomain(id=1, userName=Van, gmtBroth=2022-02-10 10:32:25, balance=100)
    }

```
 - 注意
1. 一旦使用`Converter`，`BeanCopier`只使用`Converter`定义的规则去拷贝属性，所以在`convert()`方法中要考虑所有的属性；

2. 毫无疑问，使用`Converter`会使对象拷贝速度变慢。

## 3.3 缓存BeanCopier实例提升性能

`BeanCopier`拷贝速度快，性能瓶颈出现在创建`BeanCopier`实例的过程中。 所以，把创建过的`BeanCopier`实例放到缓存中，下次可以直接获取，提升性能。

 - 测试代码

```java
@Test
public void beanCopierWithCache() {

    List<UserDO> userDOList = DataUtil.createDataList(10000);
    long start = System.currentTimeMillis();
    List<UserDTO> userDTOS = new ArrayList<>();
    userDOList.forEach(userDO -> {
        UserDTO userDTO = new UserDTO();
        copy(userDO, userDTO);
        userDTOS.add(userDTO);
    });
}

/**
 * 缓存 BeanCopier
 */
private static final ConcurrentHashMap<String, BeanCopier> BEAN_COPIERS = new ConcurrentHashMap<>();

public void copy(Object srcObj, Object destObj) {
    String key = genKey(srcObj.getClass(), destObj.getClass());
    BeanCopier copier = null;
    if (!BEAN_COPIERS.containsKey(key)) {
        copier = BeanCopier.create(srcObj.getClass(), destObj.getClass(), false);
        BEAN_COPIERS.put(key, copier);
    } else {
        copier = BEAN_COPIERS.get(key);
    }
    copier.copy(srcObj, destObj, null);

}
private String genKey(Class<?> srcClazz, Class<?> destClazz) {
    return srcClazz.getName() + destClazz.getName();
}
```

## 3.3 BeanCopier总结

 1. 当源类和目标类的属性名称、类型都相同，拷贝没问题。
 2. 当源对象和目标对象的属性名称相同、类型不同,那么名称相同而类型不同的属性不会被拷贝。注意，原始类型（`int`，`short`，`char`）和
    他们的包装类型，在这里都被当成了不同类型，因此不会被拷贝。
 3. 源类或目标类的`setter`比`getter`少，拷贝没问题，此时setter多余，但是不会报错。
 4. 源类和目标类有相同的属性（两者的`getter`都存在），但是目标类的`setter`不存在，此时会抛出`NullPointerException`。
 5. 加缓存可以提升拷贝速度。

# 四、Orika

`Orika` 是 `Java Bean` 映射框架，可以实现从一个对象递归拷贝数据至另一个对象。它的优点是：名字相同类型不同也能直接复制。

## 4.1 所需依赖

```xml
<dependency>
    <groupId>ma.glasnost.orika</groupId>
    <artifactId>orika-core</artifactId>
    <version>1.5.4</version>
</dependency>
```

## 4.2 映射工具类

使用枚举实现的单例模式创建一个映射工具类，便于测试。

```java
public enum MapperUtils {

    /**
     * 实例
     */
    INSTANCE;

    /**
     * 默认字段工厂
     */
    private static final MapperFactory MAPPER_FACTORY = new DefaultMapperFactory.Builder().build();

    /**
     * 默认字段实例
     */
    private static final MapperFacade MAPPER_FACADE = MAPPER_FACTORY.getMapperFacade();

    /**
     * 默认字段实例集合
     */
    private static Map<String, MapperFacade> CACHE_MAPPER_FACADE_MAP = new ConcurrentHashMap<>();

    /**
     * 映射实体（默认字段）
     *
     * @param toClass 映射类对象
     * @param data    数据（对象）
     * @return 映射类对象
     */
    public <E, T> E map(Class<E> toClass, T data) {
        return MAPPER_FACADE.map(data, toClass);
    }

    /**
     * 映射实体（自定义配置）
     *
     * @param toClass   映射类对象
     * @param data      数据（对象）
     * @param configMap 自定义配置
     * @return 映射类对象
     */
    public <E, T> E map(Class<E> toClass, T data, Map<String, String> configMap) {
        MapperFacade mapperFacade = this.getMapperFacade(toClass, data.getClass(), configMap);
        return mapperFacade.map(data, toClass);
    }

    /**
     * 映射集合（默认字段）
     *
     * @param toClass 映射类对象
     * @param data    数据（集合）
     * @return 映射类对象
     */
    public <E, T> List<E> mapAsList(Class<E> toClass, Collection<T> data) {
        return MAPPER_FACADE.mapAsList(data, toClass);
    }


    /**
     * 映射集合（自定义配置）
     *
     * @param toClass   映射类
     * @param data      数据（集合）
     * @param configMap 自定义配置
     * @return 映射类对象
     */
    public <E, T> List<E> mapAsList(Class<E> toClass, Collection<T> data, Map<String, String> configMap) {
        T t = data.stream().findFirst().orElseThrow(() -> new ExceptionInInitializerError("映射集合，数据集合为空"));
        MapperFacade mapperFacade = this.getMapperFacade(toClass, t.getClass(), configMap);
        return mapperFacade.mapAsList(data, toClass);
    }

    /**
     * 获取自定义映射
     *
     * @param toClass   映射类
     * @param dataClass 数据映射类
     * @param configMap 自定义配置
     * @return 映射类对象
     */
    private <E, T> MapperFacade getMapperFacade(Class<E> toClass, Class<T> dataClass, Map<String, String> configMap) {
        String mapKey = dataClass.getCanonicalName() + "_" + toClass.getCanonicalName();
        MapperFacade mapperFacade = CACHE_MAPPER_FACADE_MAP.get(mapKey);
        if (Objects.isNull(mapperFacade)) {
            MapperFactory factory = new DefaultMapperFactory.Builder().build();
            ClassMapBuilder classMapBuilder = factory.classMap(dataClass, toClass);
            configMap.forEach(classMapBuilder::field);
            classMapBuilder.byDefault().register();
            mapperFacade = factory.getMapperFacade();
            CACHE_MAPPER_FACADE_MAP.put(mapKey, mapperFacade);
        }
        return mapperFacade;
    }
}
```

**这个工具类中主要有四个方法：**

 1. `map(Class toClass, T data)：`普通的映射实体，主要映射名称相同（类型可以不同）的字段；
 2. `map(Class toClass, T data, Map<String, String>configMap)`：自定义配置的映射，映射名称不同时，自定义映射对应名称；
 3. `mapAsList(Class toClass, Collection data)`：普通的集合的映射；
 4. `mapAsList(Class toClass, Collection data, Map<String, String>configMap)`：自定义的集合映射，自定义映射对应名称。

## 4.3 简单测试
UserDO
```java
@Data
public class UserDO {

    private int id;
    private String userName;
    private int sex;
    /**
     * 以下两个字段用户模拟自定义转换
     */
    private LocalDateTime gmtBroth;
    private BigDecimal balance;

    public UserDO(Integer id, String userName, int sex, LocalDateTime gmtBroth, BigDecimal balance) {
        this.id = id;
        this.userName = userName;
        this.sex = sex;
        this.gmtBroth = gmtBroth;
        this.balance = balance;
    }
}
```
UserDTO 
```java
@Data
public class UserDTO {
    private int id;
    private String userName;
    private Integer sex;
}
```

**拷贝名称相同类型可不同的属性**

```java
@Test
public void normalCopy() {
    // 模拟查询出数据
    UserDO userDO = DataUtil.createData();
    log.info("拷贝前：userDO:{}", userDO);
    // 第一个参数：源对象， 第二个参数：目标对象，第三个参数：是否使用自定义转换器（下面会介绍），下同
    UserDTO userDTO = MapperUtils.INSTANCE.map(UserDTO.class, userDO);;
    log.info("拷贝后：userDTO:{}", userDTO);
    //拷贝前：userDO:UserDO(id=1, userName=Van, sex=0, gmtBroth=2022-02-10T14:50:16.670, balance=100)
    //拷贝后：userDTO:UserDTO(id=1, userName=Van, sex=0)
}
字段名称不同，带翻译
@Test
public void converterTest() {
    // 模拟查询出数据
    UserDO userDO = DataUtil.createData();
    Map<String, String> config = new HashMap<>();
    // 自定义配置(balance 转 balances)
    config.put("balance", "balances");
    log.info("拷贝前：userDO:{}", userDO);
    UserDomain userDomain = MapperUtils.INSTANCE.map(UserDomain.class, userDO, config);
    log.info("拷贝后：userDomain:{}", userDomain);
    //拷贝前：userDO:UserDO(id=1, userName=Van, sex=0, gmtBroth=2022-02-10T14:53:41.568, balance=100)
    //拷贝后：userDomain:UserDomain(id=1, userName=Van, gmtBroth=2022-02-10T14:53:41.568, balances=100)
}
拷贝集合
@Test
public void beanCopierWithCache() {
    List<UserDO> userDOList = DataUtil.createDataList(3);
    log.info("拷贝前：userDOList:{}", userDOList);
    List<UserDTO> userDTOS = MapperUtils.INSTANCE.mapAsList(UserDTO.class,userDOList);
    log.info("拷贝后：userDTOS:{}", userDTOS);
        //拷贝前：userDOList:[UserDO(id=1, userName=Van, sex=1, gmtBroth=2022-02-10T14:54:51.368, balance=100),
        // UserDO(id=2, userName=Van, sex=1, gmtBroth=2022-02-10T14:54:51.369, balance=100), 
        // UserDO(id=3, userName=Van, sex=1, gmtBroth=2022-02-10T14:54:51.369, balance=100)]
        
        //拷贝后：userDTOS:[UserDTO(id=1, userName=Van, sex=1), 
        // UserDTO(id=2, userName=Van, sex=1),
        // UserDTO(id=3, userName=Van, sex=1)]
}
```

# 五、[MapStruct](https://blog.csdn.net/weixin_43811057/article/details/120226901?spm=1001.2014.3001.5501)

`MapStruct` 是一个自动生成 `bean` 映射类的代码生成器。`MapStruct` 还能够在不同的数据类型之间进行转换。

## 5.1 所需依赖

 - `mapstruct-jdk8`

包含所需的注释，例如`@Mapping`。

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-jdk8</artifactId>
    <version>1.3.0.Final</version>
</dependency>
```

 - `mapstruct-processor`

在编译，生成映射器实现的注释处理器。

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.3.0.Final</version>
    <scope>provided</scope>
</dependency>
```

## 5.2 如何使用？

您所要做的就是定义一个`mapper`接口，该接口声明任何所需的映射方法。在编译期间，`MapStruct`将生成此接口的实现。此实现使用普通的`Java`方法调用来在源对象和目标对象之间进行映射。

 - 创建Mapper

利用`@Mapper`注解标注该接口/抽象类是被MapStruct自动映射的，只有存在该注解才会将内部的接口方法自动实现。

 - 获取Mapper

`MapStruct`为我们提供了多种的获取`Mapper`的方式，习惯用默认配置：采用`Mappers`通过动态工厂内部反射机制完成`Mapper`实现类的获取。

```java
UserConvertUtils INSTANCE = Mappers.getMapper(UserConvertUtils.class);
```

完整的一个转换器demo:

```java
@Mapper
public interface UserConvertUtils {

    UserConvertUtils INSTANCE = Mappers.getMapper(UserConvertUtils.class);

    /**
     * 普通的映射
     *
     * @param userDO UserDO数据持久层类
     * @return 数据传输类
     */
    UserDTO doToDTO(UserDO userDO);

    /**
     * 类型转换的映射
     *
     * @param userDO UserDO数据持久层类
     * @return 数据传输类
     */
    @Mappings({
            @Mapping(target = "gmtBroth", source = "gmtBroth", dateFormat = "yyyy-MM-dd HH:mm:ss"),
            @Mapping(target = "balances", source = "balance"),
    })
    UserDTO doToDtoWithConvert(UserDO userDO);
}
测试
/**
 * 一般拷贝
 */
@Test
public void normalCopy() {
    // 模拟查询出数据
    UserDO userDO = DataUtil.createData();
    log.info("拷贝前：userDO:{}", userDO);
    UserDTO userDTO = UserConvertUtils.INSTANCE.doToDTO(userDO);
    log.info("拷贝后：userDTO:{}", userDTO);
}
/**
 * 包含类型转换的拷贝
 */
@Test
public void doToDtoWithConvert() {
    // 模拟查询出数据
    UserDO userDO = DataUtil.createData();
    log.info("拷贝前：userDO:{}", userDO);
    UserDTO userDTO = UserConvertUtils.INSTANCE.doToDtoWithConvert(userDO);
    log.info("拷贝后：userDTO:{}", userDTO);
}
```

**打印映射结果**

```java
一般拷贝:
...拷贝前：userDO:UserDO(id=1, userName=Van, gmtBroth=2020-04-21T21:38:39.376, balance=100)
...拷贝后：userDTO:UserDTO(id=1, userName=Van, gmtBroth=2020-04-21T21:38:39.376, balances=null)
包含类型转换的拷贝:
...拷贝前：userDO:UserDO(id=1, userName=Van, gmtBroth=2020-04-21T21:05:19.282, balance=100)
...拷贝后：userDTO:UserDTO(id=1, userName=Van, gmtBroth=2020-04-21 21:05:19, balances=100)
```

通过打印结果可以发现：相较于前者，包含类型转换的拷贝可以自定义转换属性和时间格式等。

## 5.3 MapStruct 注解的关键词

 1. `@Mapper`：只有在接口加上这个注解， MapStruct 才会去实现该接口；
 2. `@Mappings`：配置多个@Mapping；
 3. `@Mapping`：属性映射，若源对象属性与目标对象名字一致，会自动映射对应属性：
 4. `source`：源属性；
 5. `target`：目标属性；
 6. `dateFormat`：字符串与日期之间相互转换；
 7. `ignore`: 忽略这个，某个属性不想映射，可以加个 ignore=true；

## 5.4 多对一

`MapStruct` 可以将几种类型的对象映射为另外一种类型，比如将多个 `DO` 对象转换为 `DTO`。

详见：

```java
UserDTO doAndInfoToDto(UserDO userDO, UserInfoDO userInfoDO);
```

## 5.5 为什么要用 MapStruct

与手工编写映射代码相比，`MapStruct`通过生成繁琐且易于编写的代码来节省时间。遵循约定优于配置方法，`MapStruct`使用合理的默认值，但在配置或实现特殊行为时会采取措施。

与动态映射框架相比，`MapStruct`具有以下优势：

 1. 通过使用普通方法调用而不是反射来快速执行
 2. 编译时类型安全：只能映射相互映射的对象和属性，不会将订单实体意外映射到客户DTO等。
 3. 在构建时清除错误报告，如果映射不完整（并非所有目标属性都已映射）映射不正确（找不到合适的映射方法或类型转换）

## 5.6 示例代码

该部分测试代码
完整代码地址

# 六、更多

通过四种属性拷贝的方式，加上自己手动`get/set`，仅给出以下建议：

 1. 简单拷贝直接使用get/set；
 2. 属性值过多的拷贝且已经使用Spring的情况下，使用BeanUtils;
 3. 属性拷贝比较麻烦，存在转译且对拷贝速度有要求时使用MapStruct(性能几乎等同于直接get/set)。

具体性能，参考文章：[Java Bean Copy 性能大比拼](https://juejin.cn/post/6844903809215381511)
