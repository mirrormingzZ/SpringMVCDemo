package cn.mirrorming.springMVCdemo.framework.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.mirrorming.springMVCdemo.framework.annotation.MirrorAutowired;
import cn.mirrorming.springMVCdemo.framework.annotation.MirrorController;
import cn.mirrorming.springMVCdemo.framework.annotation.MirrorRequestMapping;
import cn.mirrorming.springMVCdemo.framework.annotation.MirrorRequestParam;
import cn.mirrorming.springMVCdemo.framework.annotation.MirrorService;

/**
 * @author mirror
 * @version 创建时间：2018年11月1日 下午4:57:49
 * 
 */
public class DispatcherServlet extends HttpServlet {
	// 保存所有扫描包的list
	List<String> classNames = new ArrayList<>();
	// 创建一个容器map保存注解后面的值为key,对象为value
	Map<String, Object> beans = new HashMap<>();
	// 创建一个容器map存放路径方法
	Map<String, Object> handlerMap = new HashMap<>();

	// <load-on-startup>0</load-on-startup>由于web.xml中配置了此项,tomcat启动时会先执行这个方法
	public void init(ServletConfig servletConfig) {
		// 1.扫描cn.mirrorming.mirrorming_springMVCdemo.controller
		doScan("cn.mirrorming");
		// 2.实例化
		doInstance();
		// 3.注入
		doAutowired();
		// 4.匹配路径
		urlMapping();// 路径/controller/select-->method
	}

	public void urlMapping() {
		for (Map.Entry<String, Object> entry : beans.entrySet()) {
			Object instance = entry.getValue();
			Class<?> clazz = instance.getClass();
			if (clazz.isAnnotationPresent(MirrorController.class)) {
				MirrorRequestMapping requestMapping = clazz.getAnnotation(MirrorRequestMapping.class);
				String classPath = requestMapping.value(); // 获得类路径 /controller
				Method[] methods = clazz.getMethods();
				for (Method method : methods) {
					if (method.isAnnotationPresent(MirrorRequestMapping.class)) {
						MirrorRequestMapping request = method.getAnnotation(MirrorRequestMapping.class);
						String methodPath = request.value();
						handlerMap.put(classPath + methodPath, method);
					} else {
						continue;
					}
				}
			}
		}
	}

	// 注入
	public void doAutowired() {
		for (Map.Entry<String, Object> entry : beans.entrySet()) {
			Object instance = entry.getValue();
			Class<?> clazz = instance.getClass();
			if (clazz.isAnnotationPresent(MirrorController.class)) {
				Field[] fields = clazz.getFields();
				// Annotation[] annotations = clazz.getAnnotations();
				for (Field field : fields) {
					if (field.isAnnotationPresent(MirrorAutowired.class)) {
						MirrorAutowired autowired = field.getAnnotation(MirrorAutowired.class);
						String key = autowired.value();
						Object value = beans.get(key);
						field.setAccessible(true);
						try {
							field.set(instance, value);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					} else {
						continue;
					}
				}
			} else {
				continue;
			}
		}
	}

	// 实例化并添加到容器
	public void doInstance() {
		for (String className : classNames) {
			String cn = className.replace(".class", "");
			try {
				Class<?> clazz = Class.forName(cn);
				if (clazz.isAnnotationPresent(MirrorController.class)) {
					Object controllerInstance = clazz.newInstance();// 实例化对象
					// map.put(key,instance);
					MirrorRequestMapping mapping = clazz.getAnnotation(MirrorRequestMapping.class);
					String controllerKey = mapping.value();
					beans.put(controllerKey, controllerInstance);
				} else if (clazz.isAnnotationPresent(MirrorService.class)) {
					Object serviceInstance = clazz.newInstance();// 实例化对象
					// map.put(key,instance);
					MirrorService mapping = clazz.getAnnotation(MirrorService.class);
					String serviceKey = mapping.value();
					beans.put(serviceKey, serviceInstance);
				} else {
					continue;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	// 扫描包
	public void doScan(String basePackage) {
		// 扫描编译好的所有类路径
		// 将cn.mirrorming变成cn/mirrorming
		URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.", "/"));
		// URL url = this.getClass().getClassLoader().getResource("/");

		String fileStr = (url.getFile()).substring(1).replaceAll("\\%[20]+", " "); // cn.mirror

		File file = new File(fileStr);// 得到文件对象

		String[] filesStr = file.list();// 得到文件夹下所有.class
		for (String path : filesStr) {
			File filePath = new File(fileStr + path);// cn/mirrorming/mirrorming_springMVCdemo
			if (filePath.isDirectory()) {
				doScan(basePackage + "." + path);
			} else {
				// 找到class类 cn/mirrorming/mirrorming_springMVCdemo/.../xxx.class
				classNames.add(basePackage + "." + filePath.getName());
			}
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 获取到请求路径
		String uri = req.getRequestURI(); // 项目名/controller/select
		String context = req.getContextPath(); // 项目名
		String path = uri.replace(context, ""); // /controller/select--->key
		Method method = (Method) handlerMap.get(path); // /controller/select--->method
		Object args[] = hand(req, resp, method);
		Object instance = beans.get("/" + path.split("/")[1]);

		try {
			method.invoke(instance, args);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		// super.doPost(req, resp);
	}

	public static Object[] hand(HttpServletRequest request, HttpServletResponse response, Method method) {
		// 拿到当前执行的方法有哪些参数
		Class<?>[] paramClazzs = method.getParameterTypes();
		// 根据参数的个数,new一个参数的数组,将方法里的所有参数赋值到args来
		Object[] args = new Object[paramClazzs.length];
		int args_i = 0;
		int index = 0;
		for (Class<?> paramClazz : paramClazzs) {
			if (ServletRequest.class.isAssignableFrom(paramClazz)) {
				args[args_i++] = request;
			}
			if (ServletResponse.class.isAssignableFrom(paramClazz)) {
				args[args_i++] = response;
			}
			// 从0-3判断有没有requestParam注解,很明显paramClazz为0和1时,不是
			// 当为2和3为requestParam,需要解析
			Annotation[] paramAns = method.getParameterAnnotations()[index];
			if (paramAns.length > 0) {
				for (Annotation paramAn : paramAns) {
					if (MirrorRequestParam.class.isAssignableFrom(paramAn.getClass())) {
						MirrorRequestParam rp = (MirrorRequestParam) paramAn;
						String value = rp.value();
						// 找到注解里的name和age
						args[args_i++] = request.getParameter(rp.value());
					}
				}
			}
			index++;
		}
		return args;
	}
}
