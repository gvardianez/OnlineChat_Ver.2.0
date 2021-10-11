package for_hw_6;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class HomeWork6Test {

    private static Stream<Arguments> dataAfterLastTest() {
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(new Integer[]{1, 3, 1, 2, 4, 2}, new Integer[]{2}));
        list.add(Arguments.arguments(new Integer[]{1, 5, 1, 2, 4, 2, 44, 1, 1}, new Integer[]{2, 44, 1, 1}));
        list.add(Arguments.arguments(new Integer[]{45, 45, 74, 0, 1, 4, 4, 4, 2, 5, 7, 6}, new Integer[]{2, 5, 7, 6}));
        list.add(Arguments.arguments(new Integer[]{4}, new Integer[]{}));
        list.add(Arguments.arguments(new Integer[]{4, 3, 1, 2, 4, 4}, new Integer[]{}));
        return list.stream();
    }

    private static Stream<Arguments> dataCheckArray() {
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(new Integer[]{-1, 3, 500, 2, 0, 2}, false));
        list.add(Arguments.arguments(new Integer[]{1, 5, 1, 2, 4, 2, 44, 1, 1}, true));
        list.add(Arguments.arguments(new Integer[]{}, false));
        list.add(Arguments.arguments(new Integer[]{4}, true));
        list.add(Arguments.arguments(new Integer[]{0, 0, 0, 0, 0, 0, 0}, false));
        return list.stream();
    }

    @ParameterizedTest
    @MethodSource("dataCheckArray")
    void checkArrayTest(Integer[] array, boolean result) {
        Assertions.assertEquals(HomeWork6.checkArray(array), result);
    }

    @ParameterizedTest()
    @MethodSource("dataAfterLastTest")
    void arrayAfterLastFourTest(Integer[] array, Integer[] arrayResult) {
        Assertions.assertArrayEquals(HomeWork6.arrayAfterLastFour(array), arrayResult);
        Assertions.assertDoesNotThrow(()->HomeWork6.arrayAfterLastFour(array));
        Assertions.assertThrows(RuntimeException.class, () -> HomeWork6.arrayAfterLastFour(new Integer[]{0, 5, 445, 454, 1, 2, 45, 8, 5}));
    }


}