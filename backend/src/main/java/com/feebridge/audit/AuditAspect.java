package com.feebridge.audit;

import com.feebridge.common.tenant.TenantContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Records an audit entry for every state-changing controller call (POST/PUT/PATCH/DELETE),
 * capturing who acted (from the security context) and which entity id was targeted. Read-only
 * GETs are not audited. Failures here never break the request.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(
            "@annotation(org.springframework.web.bind.annotation.PostMapping) "
                    + "|| @annotation(org.springframework.web.bind.annotation.PutMapping) "
                    + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) "
                    + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void auditMutation(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String controller = signature.getDeclaringType().getSimpleName();
            String action = controller + "." + signature.getName();
            String entityType = controller.replace("Controller", "");
            String entityId = firstIdArg(joinPoint.getArgs());
            auditService.record(TenantContext.getSchoolId(), TenantContext.getUserId(), action, entityType,
                    entityId, null, null, clientIp());
        } catch (Exception ex) {
            log.debug("Audit logging skipped: {}", ex.getMessage());
        }
    }

    private String firstIdArg(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long id) {
                return id.toString();
            }
        }
        return null;
    }

    private String clientIp() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs == null ? null : attrs.getRequest().getRemoteAddr();
        } catch (Exception ex) {
            return null;
        }
    }
}
