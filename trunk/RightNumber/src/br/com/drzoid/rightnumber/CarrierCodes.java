package br.com.drzoid.rightnumber;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Carrier code processing and number reformatting.
 *
 * @author rdamazio
 */
public class CarrierCodes {

  private final Context context;
  private final PhoneNumberUtil phoneNumberUtil;
  private final SharedPreferences preferences;

  public CarrierCodes(Context context, PhoneNumberUtil phoneNumberUtil) {
    this.context = context;
    this.phoneNumberUtil = phoneNumberUtil;
    this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  /**
   * Returns whether the given country requires a carrier code for making
   * national calls.
   */
  public boolean requiresCarrier(String countryCode) {
    return getCarrierCodeResourceId(countryCode, true) != 0;
  }

  /**
   * Returns whether the given country requires a carrier code for making
   * international calls.
   */
  public boolean requiresInternationalCarrier(String countryCode) {
    return getCarrierCodeResourceId(countryCode, false) != 0;
  }

  /**
   * Reformats the number for dialing it from the given country,
   * adding a carrier code if necessary.
   *
   * @param parsedOriginalNumber the original number dialed
   * @param newNumber the number after basic formatting
   * @param dialingFrom the country we're dialing from
   * @return the reformatted number
   */
  public String reformatNumberForCountry(PhoneNumber parsedOriginalNumber, String newNumber, String dialingFrom) {
    boolean nationalDialing = isNationalDialing(parsedOriginalNumber, dialingFrom);

    if (nationalDialing && !requiresCarrier(dialingFrom)) {
      // National dialing, no carrier required
      return newNumber;
    }

    if (!nationalDialing && !requiresInternationalCarrier(dialingFrom)) {
      // International dialing, no carrier required
      return newNumber;
    }

    // Check if carrier use is enabled for the dialing country
    if (!preferences.getBoolean(RightNumberConstants.ENABLE_CARRIER_BASE_KEY + dialingFrom.toLowerCase(), false)) {
      return newNumber;
    }

    // Get the carrier code to use
    String carrierCode = getCarrierCode(dialingFrom, nationalDialing);

    // If there's a +, remove it
    newNumber = newNumber.replace('+', ' ');

    // Prepend the carrier code
    newNumber = carrierCode + newNumber;
    return newNumber;
  }


  /**
   * Gets the carrier code the user asked to use for a given country.
   *
   * @param dialingFrom the country we're dialing from
   * @param nationalDialing whether the number dialed is from the same country
   * @return
   */
  private String getCarrierCode(String dialingFrom, boolean nationalDialing) {
    // Check whether to use a national or international carrier code
    StringBuilder carrierCodePreferenceKeyBuilder = new StringBuilder();
    if (!nationalDialing) {
      carrierCodePreferenceKeyBuilder.append("int_");
    }

    // Get the proper carrier code to use
    carrierCodePreferenceKeyBuilder.append("carrier_");
    carrierCodePreferenceKeyBuilder.append(dialingFrom.toLowerCase());
    String carrierCodePreferenceKey = carrierCodePreferenceKeyBuilder.toString();
    String defaultCarrierCode = getDefaultCarrierCode(dialingFrom, nationalDialing);
    String carrierCode = preferences.getString(carrierCodePreferenceKey, defaultCarrierCode);
    return carrierCode;
  }

  /**
   * Returns the default carrier code for dialing from a country.
   *
   * @param dialingFrom the country we're dialing from
   * @param nationalDialing whether the number dialed is from the same country
   * @return the carrier code
   */
  public String getDefaultCarrierCode(String dialingFrom, boolean nationalDialing) {
    int id = getCarrierCodeResourceId(dialingFrom, nationalDialing);
    if (id == 0) {
      return null;
    }
    String[] carrierCodes = context.getResources().getStringArray(id);
    return carrierCodes[0];
  }

  /**
   * Returs the resource ID for the string array of carrier codes for the given
   * call.
   *
   * @param dialingFrom the country we're dialing from
   * @param nationalDialing whether the number dialed is from the same country
   * @return the resource ID, or 0 if not found
   */
  private int getCarrierCodeResourceId(String dialingFrom, boolean nationalDialing) {
    StringBuilder codeArraysName = new StringBuilder();
    if (!nationalDialing) {
      codeArraysName.append("int_");
    }
    codeArraysName.append("carrier_codes_");
    codeArraysName.append(dialingFrom.toLowerCase());

    int id = context.getResources().getIdentifier(codeArraysName.toString(), "array",
        RightNumberConstants.RES_PACKAGE);
    return id;
  }

  /**
   * Checks whether this will be a national call - i.e. if the number being
   * dialed is from the same country that we're dialing from.
   *
   * @param number the number to check
   * @param dialingFrom the country we're dialing from
   * @return true if it's a national call, false otherwise
   */
  private boolean isNationalDialing(PhoneNumber number, String dialingFrom) {
    return (number.getCountryCode() == phoneNumberUtil.getCountryCodeForRegion(dialingFrom));
  }
}