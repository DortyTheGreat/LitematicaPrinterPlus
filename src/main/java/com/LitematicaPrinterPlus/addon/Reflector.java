package com.LitematicaPrinterPlus.addon;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflector {
    
    public static Object invokePrivateMethod(Object obj, String methodName, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        
        // Получение класса объекта
        Class<?> clazz = obj.getClass();
        
        // Поиск метода по имени и параметрам
        Method privateMethod = findMatchingMethod(clazz, methodName, args);
        
        if (privateMethod == null) {
            throw new NoSuchMethodException("Метод " + methodName + " не найден.");
        }
        
        // Открываем доступ к приватному методу
        privateMethod.setAccessible(true);
        
        // Вызов метода
        return privateMethod.invoke(obj, args);
    }

    /**
     * Функция поиска подходящего метода по имени и аргументам.
     */
    private static Method findMatchingMethod(Class<?> clazz, String methodName, Object... args) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                
                // Проверка соответствия типов параметров
                if (parameterTypes.length == args.length && matchParameters(parameterTypes, args)) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Проверяет соответствие типов параметров.
     */
    private static boolean matchParameters(Class<?>[] parameterTypes, Object... args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }
}