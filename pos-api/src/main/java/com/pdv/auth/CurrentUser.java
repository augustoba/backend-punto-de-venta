package com.pdv.auth;

/** Usuario de la petición en curso (lo fija el filtro). Fuera de una petición devuelve "sistema". */
public final class CurrentUser {
    private static final ThreadLocal<TokenService.Claims> HOLDER = new ThreadLocal<>();
    private CurrentUser() {}

    static void set(TokenService.Claims c) { HOLDER.set(c); }
    static void clear() { HOLDER.remove(); }
    public static String name() { TokenService.Claims c = HOLDER.get(); return c == null ? "sistema" : c.username(); }
    public static boolean isAdmin() { TokenService.Claims c = HOLDER.get(); return c != null && c.role() == AppUser.Role.ADMIN; }
}
