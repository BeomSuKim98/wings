<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${title}"/></title>  <!-- 컨트롤러가 넣어준 제목 -->
    <link rel="stylesheet" href="<c:url value='/css/common/header.css'/>">
</head>
<body>
<!-- 헤더 조각을 항상 포함 -->
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="content">
    <!-- 본문 조각: controller가 넘긴 body를 실제 경로로 풀어서 include -->
    <jsp:include page="${body}"/>
</main>

<!--<jsp:include page="/WEB-INF/views/common/footer.jsp"/>-->
</body>
</html>