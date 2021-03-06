/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package br.com.drzoid.rightnumber;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Agent for backing up and restoring preferences to/from "the cloud".
 *
 * @author rdamazio
 */
public class RightNumberBackupAgent extends BackupAgentHelper {
  private static final String PREFERENCES = RightNumberConstants.RES_PACKAGE + "_preferences";

  @Override
  public void onCreate() {
    super.onCreate();

    addHelper("prefs", new SharedPreferencesBackupHelper(this, PREFERENCES));
  }
}
