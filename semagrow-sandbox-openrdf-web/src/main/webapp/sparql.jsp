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
        <h1>SPARQL</h1>
        <form action="sparql">
            <textarea cols="70" rows="10" name="query">SELECT * WHERE { ?s ?p ?o } LIMIT 100</textarea>
            <br/>
            <select name="acceptMimeType">                
                
                <optgroup label="SELECT">
                <%
                for(TupleQueryResultFormat format : TupleQueryResultFormat.values()){
                    if(format.getDefaultMIMEType().indexOf("binary")==-1){
                    %>
                    <option value="<%=format.getDefaultMIMEType()%>"><%=format.getName()%></option>
                    <%   
                    }                                    
                }
                %>
                </optgroup>
                
                <optgroup label="CONSTRUCT">
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
                
                <optgroup label="ASK">
                <%
                for(BooleanQueryResultFormat format : BooleanQueryResultFormat.values()){
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
