<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">  
<html>  
<head>  
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">  
<title>Insert title here</title>  
<script type="text/javascript" src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>  

</head>  
<body>  
<form id="formId" action="/upload" target="frame1" method="POST" enctype="multipart/form-data">  
    <input type="file" name="file"/>  
    <input type="button" value="提交" onclick="upload()">  
</form>  

<iframe name="frame1" frameborder="0" height="40"></iframe>

<script type="text/javascript">
    function upload() {
        $("#formId").submit();
    }
</script>

</body>  
</html>