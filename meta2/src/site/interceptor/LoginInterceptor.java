package site.interceptor;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import site.model.UserBean;

import java.util.Map;

/**
 * Classe do interceptor de editor.
 * Verifica antes de cada ação que precise de permissoes de editor se o utilizador as tem.
 * Caso nao tenha, é redirecionado para a página de login.
 */
public class LoginInterceptor implements Interceptor {
    private static final long serialVersionUID = 189237412378L;

    /**
     * Metodo que intercepta a acao.
     *
     * @param invocation invocacao da acao
     * @return Login ou a proxima acao
     * @throws Exception exception
     */
    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Map<String, Object> session = invocation.getInvocationContext().getSession();
        boolean loggedin = (boolean) session.getOrDefault("loggedin", false);

        System.out.println("[LoginInterceptor] " + session.keySet());

        if (!loggedin) {
            System.out.println("Manitor nao tem login feito, vai masé fazer login, pato.");
            return Action.LOGIN;
        } else {
            UserBean userBean = (UserBean) session.get("userBean");
            if (userBean != null) {
                session.put("editor", userBean.getUtilizador().getIsEditor());
            }
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