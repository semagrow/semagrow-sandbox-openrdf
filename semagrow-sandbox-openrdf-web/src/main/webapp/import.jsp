<%@page import="org.openrdf.query.resultio.BooleanQueryResultFormat"%>
<%@page import="org.openrdf.query.resultio.TupleQueryResultFormat"%>
<%@page import="org.openrdf.rio.RDFFormat"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Sandbox-Admin</title>
    </head>
    <body>
        <h1>IMPORT</h1>
        <form action="import" method="post">
            <textarea cols="70" rows="10" name="rdf"></textarea>
            <br/>
            <select name="mimeType">                
                <optgroup label="RDF">
                <%
                for(RDFFormat format : RDFFormat.values()){
                    if(format.getDefaultMIMEType().indexOf("binary")==-1){
                    %>
                    <option value="<%=format.getDefaultMIMEType()%>"><%=format.getName()%></option>
                    <%
                    }
                }
                %>
                </optgroup>                
            </select>
            <input type="submit" value="Run Query"/>
        </form>
    </body>
</html>
