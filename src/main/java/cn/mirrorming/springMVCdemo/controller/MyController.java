package cn.mirrorming.springMVCdemo.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.mirrorming.springMVCdemo.framework.annotation.MirrorAutowired;
import cn.mirrorming.springMVCdemo.framework.annotation.MirrorController;
import cn.mirrorming.springMVCdemo.framework.annotation.MirrorRequestMapping;
import cn.mirrorming.springMVCdemo.framework.annotation.MirrorRequestParam;
import cn.mirrorming.springMVCdemo.service.IMirrorSercice;
import cn.mirrorming.springMVCdemo.service.MirrorServiceImp;

/**
 * @author mirror
 * @version 创建时间：2018年11月1日 下午4:39:12
 * 
 */
@MirrorRequestMapping("/myController")
@MirrorController("mirrorController")
public class MyController {

	@MirrorAutowired("/mirrorService")
	public MirrorServiceImp mirrorSercice;

	@MirrorRequestMapping("/select")
	public void select(HttpServletRequest request, HttpServletResponse response,
			@MirrorRequestParam("name") String name, @MirrorRequestParam("age") String age) {

		PrintWriter printWriter;
		try {
			printWriter = response.getWriter();
			String result = mirrorSercice.select(name, age);
			printWriter.write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
