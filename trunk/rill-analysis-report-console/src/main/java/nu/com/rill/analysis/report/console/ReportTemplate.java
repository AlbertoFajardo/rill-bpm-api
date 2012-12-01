package nu.com.rill.analysis.report.console;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class ReportTemplate {

    @NotNull
    @Column(unique = true)
    private String name;

    @NotNull
    private String creator;

    @NotNull
    private String belongToApp;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date createDate;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @NotNull
    private byte[] contents;
    
}
