package ma.glasnost.orika.test.map;

import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by shucheng on 2019-7-19 下午 21:45
 * 测试mapAsX方法（主要测试mapAsMap方法，将List<T>中实例类中的两个属性分别作为key、value转换到新的Map中）
 *
 * 相关链接：
 * https://orika-mapper.github.io/orika-docs/advanced-mappings.html
 * https://gist.github.com/elaatifi/6269684
 * https://www.cnkirito.moe/orika/
 * https://stackoverflow.com/questions/40496318/orika-mapper-map-to-liststring
 * https://blog.csdn.net/neweastsun/article/details/80559868
 * 最后还是到单元测试里找到了用法
 */
public class MapAsXTest {

    private List<MyVaribale> originList;

    @Before
    public void setUp() {
        originList = new ArrayList<MyVaribale>();
        MyVaribale myVar = new MyVaribale();
        myVar.serialNum = 101;
        myVar.name = "sname";
        myVar.value = "张三";
        originList.add(myVar);

        myVar = new MyVaribale();
        myVar.serialNum = 102;
        myVar.name = "sex";
        myVar.value = "男";
        originList.add(myVar);
    }

    @Test
    public void test() {
        Map<String, String> map = new HashMap<String, String>();
        for (MyVaribale myVaribale : originList) {
            map.put(myVaribale.getName(), myVaribale.getValue());
        }
        System.out.println(map);

        Map<Integer, String> map2 = new HashMap<Integer, String>();
        for (MyVaribale myVaribale : originList) {
            map2.put(myVaribale.getSerialNum(), myVaribale.getValue());
        }
        System.out.println(map2);
    }

    /**
     * mapAsList方法
     */
    @Test
    public void test2() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.classMap(MyVaribale.class, MyVaribale2.class)
                .field("name", "name2")
                .field("value", "value2")
                .byDefault().register();
        List<MyVaribale2> destList = mapperFactory.getMapperFacade().mapAsList(originList, MyVaribale2.class);
        System.out.println(destList);
    }

    /**
     * mapAsMap方法
     * 测试方法参考ma.glasnost.orika.test.map.CoreMappingFunctionsTestCase#testCollectionToMap_Simple()
     */
    @Test
    public void test3() throws ClassNotFoundException {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        Type<Map<String, String>> mapType = new TypeBuilder<Map<String, String>>(){}
                .build();
        System.out.println(mapType);
        // 该写法见ma.glasnost.orika.test.community.issue121.Issue121TestCase.test1
        /*Type mapType2 = TypeFactory.valueOf(Map.class, String.class, String.class);
        System.out.println(mapType2);*/
        /* 使用参数化泛型
        Type mapType3 = TypeFactory.valueOf(new HashMap<String, String>(){}.getClass().getGenericSuperclass());
        System.out.println(mapType3);*/
        Type<MapEntry<String, String>> entryType = MapEntry.concreteEntryType(mapType);

        Type<MyVaribale> myVariableType = TypeFactory.valueOf(MyVaribale.class);

        factory.registerClassMap(factory.classMap(myVariableType, entryType)
                .field("name", "key")
                .field("value", "value")
                .byDefault()
                .toClassMap());
        /*Class linkedHashMapClazz = Class.forName("java.util.LinkedHashMap$Entry");
        factory.registerConcreteType(linkedHashMapClazz, MapEntry.class);*/
        factory.registerConcreteType(Map.Entry.class, MapEntry.class);

        MapperFacade mapper = factory.getMapperFacade();
        Map<String, String> result = mapper.mapAsMap(originList, myVariableType, mapType);
        System.out.println(result);
    }

    /**
     * 测试返回LinkedHashMap（需要修改源码，不然一直返回HashMap）
     * L921 ma.glasnost.orika.impl.MapperFacadeImpl#mapAsMap(java.lang.Iterable, ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type, ma.glasnost.orika.MappingContext)
     * 中的 Map<Dk, Dv> destination = new HashMap<Dk, Dv>(); 注释掉
     * 改成：
     * Map<Dk, Dv> destination;
     * try {
     *     destination = destinationType.getRawType().newInstance();
     * } catch (Exception e) {
     *     destination = new HashMap<Dk, Dv>();
     * }
     */
    @Test
    public void testReturnLinkedHashMap() {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        Type<Map<String, String>> mapType = new TypeBuilder<Map<String, String>>(){}
                .build();
        System.out.println(mapType);
        Type<LinkedHashMap<String, String>> mapType2 = new TypeBuilder<LinkedHashMap<String, String>>(){}.build();
        // 该写法见ma.glasnost.orika.test.community.issue121.Issue121TestCase.test1
        /*Type mapType2 = TypeFactory.valueOf(Map.class, String.class, String.class);
        System.out.println(mapType2);*/
        /* 使用参数化泛型
        Type mapType3 = TypeFactory.valueOf(new HashMap<String, String>(){}.getClass().getGenericSuperclass());
        System.out.println(mapType3);*/
        Type<MapEntry<String, String>> entryType = MapEntry.concreteEntryType(mapType2);

        Type<MyVaribale> myVariableType = TypeFactory.valueOf(MyVaribale.class);

        factory.registerClassMap(factory.classMap(myVariableType, entryType)
                .field("name", "key")
                .field("value", "value")
                .byDefault()
                .toClassMap());
        /*Class linkedHashMapClazz = Class.forName("java.util.LinkedHashMap$Entry");
        factory.registerConcreteType(linkedHashMapClazz, MapEntry.class);*/
        factory.registerConcreteType(Map.Entry.class, MapEntry.class);

        MapperFacade mapper = factory.getMapperFacade();
        Map<String, String> result = mapper.mapAsMap(originList, myVariableType, mapType2);
        System.out.println(result);
    }

    /**
     * 封装后的mapAsMap方法的使用
     */
    @Test
    public void test4() {
        Map<String, String> map = mapAsMap(originList, MyVaribale.class, new LinkedHashMap<String, String>(){},
                "name", "value");
        System.out.println(map);

        Map<Integer, String> map2 = mapAsMap(originList, MyVaribale.class, new LinkedHashMap<Integer, String>(){},
                "serialNum", "value");
        System.out.println(map2);
    }

    public static <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Class<S> sourceClass, Map<Dk, Dv> destinationMap,
                                                   String keyFieldName, String valueFieldName) {
        MapperFactory factory = new DefaultMapperFactory.Builder().build();

        Type<S> sourceType = TypeFactory.valueOf(sourceClass);

        /*ParameterizedType parameterizedType = (ParameterizedType) destinationMap.getClass().getGenericSuperclass();
        Type<? extends Map<Dk, Dv>> destinationType = TypeFactory.valueOf(parameterizedType);*/
        Type<? extends Map<Dk, Dv>> destinationType = TypeFactory.valueOf(destinationMap.getClass().getGenericSuperclass());

        Type<MapEntry<Dk, Dv>> entryType = MapEntry.concreteEntryType(destinationType);

        factory.registerClassMap(factory.classMap(sourceType, entryType)
                .field(keyFieldName, "key")
                .field(valueFieldName, "value")
                .byDefault().toClassMap());
        factory.registerConcreteType(Map.Entry.class, MapEntry.class);

        MapperFacade mapper = factory.getMapperFacade();
        return mapper.mapAsMap(source, sourceType, destinationType);
    }

    public static class MyVaribale {
        private Integer serialNum;
        private String name;
        private String value;

        public Integer getSerialNum() {
            return serialNum;
        }

        public void setSerialNum(Integer serialNum) {
            this.serialNum = serialNum;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class MyVaribale2 {
        private String name2;
        private String value2;

        public String getName2() {
            return name2;
        }

        public void setName2(String name2) {
            this.name2 = name2;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }
    }
}
