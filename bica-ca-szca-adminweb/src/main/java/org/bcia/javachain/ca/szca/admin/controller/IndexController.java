/*
 *
 * Copyright © 2018  深圳市电子商务安全证书管理有限公司(SZCA,深圳CA) 版权所有
 * Copyright © 2018  SZCA. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.bcia.javachain.ca.szca.admin.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.szca.common.exception.JspViewNotFoundException;
import com.szca.common.web.ControllerExceptionHandler;

@Controller
public class IndexController {
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@RequestMapping("index")
	public ModelAndView index(HttpServletRequest request) {
		Object obj = request.getSession().getAttribute("authenticationToken");
		Object usercommonname = request.getSession().getAttribute("usercommonname");
		return new ModelAndView("index");
	}

	@RequestMapping("home")
	public ModelAndView home(HttpServletRequest request) {
		Object obj = request.getSession().getAttribute("authenticationToken");
		Object usercommonname = request.getSession().getAttribute("usercommonname");
		// CAInterfaceBean cabean = new CAInterfaceBean();
		// org.ejbca.ui.web.admin.configuration.EjbcaWebBean ejbcawebbean = new
		// org.ejbca.ui.web.admin.configuration.EjbcaWebBean();

		// request.getSession().setAttribute("cabean", cabean);
		// request.getSession().setAttribute("ejbcawebbean", ejbcawebbean);

		ModelAndView view = new ModelAndView("home");
		// view.addObject("ejbcawebbean", ejbcawebbean);
		return view;
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 *             <br>
	 *             Note:<br>
	 *             有些简单的功能不用编写单独的Controller，则在此直接跳转到对应的jsp,<br>
	 *             比如/test/testObj.html -> /views/test/testObj.jsp
	 *             /test/a/b/c/d.html -> /views/test/a/b/c/d.jsp
	 */
	@RequestMapping(value = "/**", method = { RequestMethod.POST, RequestMethod.GET })
	public String doDefault(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("doDefault:" + request.getRequestURI());
		String jspView = ControllerExceptionHandler.getView(request);
		if (ControllerExceptionHandler.checkJSP(request))
			return jspView;
		else
			// throw exception , ControllerExceptionHandler would catch this  exception
			throw new JspViewNotFoundException(String.format("View[%s] not found.", jspView));
	}

	
}
