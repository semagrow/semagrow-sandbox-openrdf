package eu.semagrow.sandbox.openrdf.api;

import java.io.IOException;
import java.io.OutputStream;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

/**
 * @author http://www.turnguard.com/turnguard
 */
public interface OpenRDFDataService {
    public void doQuery(String sparqlQuery, String acceptMimeType, OutputStream out) throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, RDFHandlerException, IOException ;
    public void importRDF(String rdfString, String baseURL, String mimeType) throws RepositoryException, IOException, RDFParseException, RDFHandlerException;
    public void addTriple(URI s, URI p, Value o) throws RepositoryException;    
    public void removeTriple(URI s, URI p, Value o) throws RepositoryException;
    public void shutDown();
}
