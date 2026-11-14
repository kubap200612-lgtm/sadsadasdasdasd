package pl.rynekanaliza;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Obejście błędu BK-Rynku 3.0, który wysypywał grę (StackOverflowError w AutoMarketController.sendFeedback).
 * <p>
 * BK czyta czat przez zdarzenie Fabric GAME i miał pomijać własne komunikaty ("[BK Client] ..."), ale sprawdza tekst
 * z kodami kolorów ("§8[§2B§aK §fClient§8]"), więc ich nie rozpoznaje. Gdy serwer odpisze, że brakuje pieniędzy na zakup,
 * BK pisze "Brak środków na zakup: X", czyta to jako kolejny brak pieniędzy i pisze znowu - w kółko, aż skończy się stos.
 * <p>
 * Tu komunikaty BK ("[BK Client]" po zdjęciu kolorów) trafiają prosto do okna czatu, z pominięciem zdarzeń czatu,
 * więc BK ich nie czyta (tak, jak sam zamierzał) - pętla się nie zaczyna, a komunikat i tak jest widoczny.
 * Komunikaty serwera, graczy i tego moda przechodzą bez zmian.
 */
final class BkChatGuard {
    static final String BK_PREFIX = "[BK Client]";

    private BkChatGuard() {}

    /** Zdarzenie ALLOW_GAME: false = komunikat BK pokazany od razu w oknie czatu, a dalsze zdarzenia czatu (BK) go nie widzą. */
    static boolean allow(Text message, boolean overlay) {
        try {
            if (overlay || message == null || !isBkMessage(message.getString())) return true;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.inGameHud == null) return true;
            mc.inGameHud.getChatHud().addMessage(message);
            return false;
        } catch (Throwable t) {
            return true; // w razie kłopotu zwykła droga komunikatu
        }
    }

    /** Czy to komunikat BK-Rynku: po zdjęciu kodów kolorów zaczyna się od "[BK Client]". */
    static boolean isBkMessage(String s) {
        return s != null && !s.isEmpty() && strip(s).startsWith(BK_PREFIX);
    }

    /** Tekst bez kodów formatowania (§ i znak po nim). */
    static String strip(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§') {
                i++;
                continue;
            }
            b.append(c);
        }
        return b.toString().trim();
    }
}
