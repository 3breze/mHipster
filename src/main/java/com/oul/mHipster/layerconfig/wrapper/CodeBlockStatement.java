package com.oul.mHipster.layerconfig.wrapper;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
public class CodeBlockStatement {

    @XmlElement
    private String statementBody;
    @XmlElementWrapper(name = "requestArgs")
    @XmlElement(name = "statementArg")
    private List<StatementArg> requestArgs;
    @XmlTransient
    private Object[] responseArgs;

    public CodeBlockStatement() {
    }

    public CodeBlockStatement(String statementBody, List<StatementArg> requestArgs) {
        this.statementBody = statementBody;
        this.requestArgs = requestArgs;
    }

    public String getStatementBody() {
        return statementBody;
    }

    @XmlTransient
    public void setStatementBody(String statementBody) {
        this.statementBody = statementBody;
    }

    public List<StatementArg> getRequestArgs() {
        return requestArgs;
    }

    @XmlTransient
    public void setRequestArgs(List<StatementArg> requestArgs) {
        this.requestArgs = requestArgs;
    }

    public Object[] getResponseArgs() {
        return responseArgs;
    }

    public void setResponseArgs(Object[] responseArgs) {
        this.responseArgs = responseArgs;
    }
}
