<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page session="false"%>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld"%>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld"%>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld"%>

<%@page import="org.nutz.mvc.impl.NutMessageMap"%>

<%
NutMessageMap msg = (NutMessageMap)request.getAttribute("msg");
Object obj = request.getAttribute("obj");
%>