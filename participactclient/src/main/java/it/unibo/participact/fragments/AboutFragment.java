/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fima.cardsui.views.CardUI;

import java.io.InputStream;

import it.unibo.participact.R;
import it.unibo.participact.views.cards.LicenseCard;

public class AboutFragment extends Fragment {

    private CardUI cardUI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_card, container, false);
        cardUI = (CardUI) root.findViewById(R.id.cardUIView);

        String license;
        try {
            Resources res = getResources();
            InputStream inStream = res.openRawResource(R.raw.licenses);

            byte[] bytes = new byte[inStream.available()];
            inStream.read(bytes);

            license = new String(bytes);
        } catch (Exception e) {
            license = getString(R.string.licenses_error);
        }

        LicenseCard licenseCard = new LicenseCard(getString(R.string.licenses_title), license);

        cardUI.addCard(licenseCard);
        cardUI.refresh();

        return root;
    }
}
