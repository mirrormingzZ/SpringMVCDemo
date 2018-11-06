package cn.mirrorming.springMVCdemo.service;

import cn.mirrorming.springMVCdemo.framework.annotation.MirrorService;

/**
 * @author mirror
 * @version 创建时间：2018年11月1日 下午4:37:32
 * 
 */
@MirrorService("/mirrorService")
public class MirrorServiceImp implements IMirrorSercice {

	@Override
	public String select(String name, String age) {
		return "name:" + name + ",age:" + age;
	}

}
