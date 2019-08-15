package site.interceptor;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

import java.util.Map;

/**
 * Classe do interceptor de login.
 * Verifica antes de cada ação, excepto login e registo, se o utilizador tem login feito.
 * Caso nao tenha login feito, é redirecionado para a página de login.
 */
public class EditorInterceptor implements Interceptor {
    private static final long serialVersionUID = 189237412378L;

    /**
     * Metodo de interecepçao da acao
     *
     * @param invocation proxima acao
     * @return resultado da acao
     * @throws Exception exception
     */
    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Map<String, Object> session = invocation.getInvocationContext().getSession();
        boolean editor = (boolean) session.getOrDefault("editor", false);

        System.out.println("[EditorInterceptor]");

        if (!editor) {
            System.out.println("Manito nao tem permissoes.");
            return Action.LOGIN;
        }

        return invocation.invoke();
    }

    /**
     * Override init()
     */
    @Override
    public void init() {
    }

    /**
     * Override destroy()
     */
    @Override
    public void destroy() {
    }
}