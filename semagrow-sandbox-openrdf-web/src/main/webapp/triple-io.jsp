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
        <h1>TRIPLE-IO</h1>
        <form action="triple-io" method="post">
            <input type="text" name="s"/>
            <input type="text" name="p"/>
            <input type="text" name="o"/>
            <select name="action">
                <option value="add">Add</option>
                <option value="remove">Remove</option>
            </select>
            <input type="submit" value="Update Triples"/>
        </form>
    </body>
</html>
