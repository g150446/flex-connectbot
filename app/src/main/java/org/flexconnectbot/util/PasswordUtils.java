/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2025 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flexconnectbot.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utility class for encrypting and decrypting host passwords.
 * Uses device-specific master key derived from Android ID for encryption.
 */
public class PasswordUtils {
    private static final String TAG = "CB.PasswordUtils";
    private static final String PREFS_NAME = "connectbot_password_prefs";
    private static final String MASTER_KEY_PREF = "master_key_salt";
    
    // Size in bytes of salt to use.
    private static final int SALT_SIZE = 8;
    
    // Number of iterations for password hashing. PKCS#5 recommends 1000
    private static final int ITERATIONS = 1000;
    
    // Cannot be instantiated
    private PasswordUtils() {
    }
    
    /**
     * Get or create a device-specific master key salt.
     * This ensures passwords are encrypted with a device-specific key.
     */
    private static byte[] getMasterKeySalt(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saltBase64 = prefs.getString(MASTER_KEY_PREF, null);
        
        if (saltBase64 != null) {
            return android.util.Base64.decode(saltBase64, android.util.Base64.DEFAULT);
        }
        
        // Generate new salt and save it
        byte[] salt = new byte[SALT_SIZE];
        try {
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
        } catch (java.security.NoSuchAlgorithmException e) {
            // Fallback to default SecureRandom
            new SecureRandom().nextBytes(salt);
        }
        String saltBase64New = android.util.Base64.encodeToString(salt, android.util.Base64.DEFAULT);
        prefs.edit().putString(MASTER_KEY_PREF, saltBase64New).apply();
        
        return salt;
    }
    
    /**
     * Get device-specific master key from Android ID and salt.
     */
    private static String getMasterKey(Context context) {
        try {
            String androidId = android.provider.Settings.Secure.getString(
                context.getContentResolver(), 
                android.provider.Settings.Secure.ANDROID_ID);
            
            // Use a combination of Android ID and package name for uniqueness
            String packageName = context.getPackageName();
            return androidId + packageName;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get Android ID, using fallback", e);
            // Fallback to a default key (less secure but better than nothing)
            return "connectbot_default_master_key_2025";
        }
    }
    
    /**
     * Encrypt a password for storage.
     * 
     * @param context Android context
     * @param password Plain text password to encrypt
     * @return Encrypted password as byte array (salt + ciphertext)
     * @throws Exception on encryption error
     */
    public static byte[] encryptPassword(Context context, String password) throws Exception {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        byte[] salt = getMasterKeySalt(context);
        String masterKey = getMasterKey(context);
        byte[] passwordBytes = password.getBytes("UTF-8");
        
        byte[] ciphertext = Encryptor.encrypt(salt, ITERATIONS, masterKey, passwordBytes);
        
        // Combine salt and ciphertext
        byte[] complete = new byte[salt.length + ciphertext.length];
        System.arraycopy(salt, 0, complete, 0, salt.length);
        System.arraycopy(ciphertext, 0, complete, salt.length, ciphertext.length);
        
        // Clear sensitive data
        Arrays.fill(salt, (byte) 0x00);
        Arrays.fill(passwordBytes, (byte) 0x00);
        Arrays.fill(ciphertext, (byte) 0x00);
        
        return complete;
    }
    
    /**
     * Decrypt a password from storage.
     * 
     * @param context Android context
     * @param encryptedPassword Encrypted password (salt + ciphertext)
     * @return Plain text password
     * @throws Exception on decryption error
     */
    public static String decryptPassword(Context context, byte[] encryptedPassword) throws Exception {
        if (encryptedPassword == null || encryptedPassword.length == 0) {
            return null;
        }
        
        byte[] salt = new byte[SALT_SIZE];
        byte[] ciphertext = new byte[encryptedPassword.length - salt.length];
        
        System.arraycopy(encryptedPassword, 0, salt, 0, salt.length);
        System.arraycopy(encryptedPassword, salt.length, ciphertext, 0, ciphertext.length);
        
        String masterKey = getMasterKey(context);
        byte[] passwordBytes = Encryptor.decrypt(salt, ITERATIONS, masterKey, ciphertext);
        
        String password = new String(passwordBytes, "UTF-8");
        
        // Clear sensitive data
        Arrays.fill(salt, (byte) 0x00);
        Arrays.fill(passwordBytes, (byte) 0x00);
        Arrays.fill(ciphertext, (byte) 0x00);
        
        return password;
    }
}

