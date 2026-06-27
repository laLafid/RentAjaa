package com.lafid.rentaja.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FormatUtils {

    // "500000" → "Rp 500.000"
    public static String formatRupiah(long amount) {
        NumberFormat fmt = NumberFormat.getInstance(new Locale("id", "ID"));
        return "Rp " + fmt.format(amount);
    }

    // "2024-06-15" → "15 Jun 2024"
    public static String formatDate(String raw) {
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", new Locale("id"));
            Date date = in.parse(raw);
            return date != null ? out.format(date) : raw;
        } catch (ParseException e) {
            return raw;
        }
    }

    // Days between two "yyyy-MM-dd" strings (min 1)
    public static int daysBetween(String start, String end) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date s = sdf.parse(start);
            Date e = sdf.parse(end);
            if (s == null || e == null) return 1;
            long diff = e.getTime() - s.getTime();
            return (int) Math.max(1, TimeUnit.MILLISECONDS.toDays(diff));
        } catch (ParseException ex) {
            return 1;
        }
    }

    // Today as "yyyy-MM-dd"
    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Tomorrow as "yyyy-MM-dd"
    public static String tomorrow() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(System.currentTimeMillis() + 86_400_000L));
    }
}
