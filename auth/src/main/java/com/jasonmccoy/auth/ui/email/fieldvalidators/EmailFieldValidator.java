/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jasonmccoy.auth.ui.email.fieldvalidators;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.Patterns;

import com.jasonmccoy.auth.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;


public class EmailFieldValidator extends BaseValidator {

    private String mInvalidDomain;

    public EmailFieldValidator(TextInputLayout errorContainer) {
        super(errorContainer);
        mErrorMessage = mErrorContainer.getResources().getString(R.string.invalid_email_address);
        mEmptyMessage = mErrorContainer.getResources().getString(R.string.missing_email_address);
        mInvalidDomain = mErrorContainer.getResources().getString(R.string.invalid_email_domain);
    }

    @Override
    protected boolean isValid(CharSequence charSequence) {
        return Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
    }

    @Override
    public boolean validate(CharSequence charSequence) {
        if (mEmptyMessage != null && (charSequence == null || charSequence.length() == 0)) {
            mErrorContainer.setError(mEmptyMessage);
            return false;
        } else if (isInvalidDomain(charSequence.toString())) {
            mErrorContainer.setError(mInvalidDomain);
            return false;
        } else if (isValid(charSequence)) {
            mErrorContainer.setError("");
            return true;
        } else {
            mErrorContainer.setError(mErrorMessage);
            return false;
        }
    }

    private boolean isInvalidDomain(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        String[] emails = loadAssetTextAsString(mErrorContainer.getContext(), "invalid_email.txt").split(",");
        return Arrays.asList(emails).contains(domain);
    }

    private String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
