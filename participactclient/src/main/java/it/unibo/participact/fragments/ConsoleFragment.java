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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fima.cardsui.views.CardUI;

import it.unibo.participact.R;
import it.unibo.participact.support.ColorUtility;
import it.unibo.participact.views.cards.ConsoleCard;
import it.unibo.participact.views.cards.ConsoleGen;

public class ConsoleFragment extends Fragment {

    CardUI _cardUI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_card, container, false);
        _cardUI = (CardUI) root.findViewById(R.id.cardUIView);

        ConsoleGen consoleGen = new ConsoleGen(getString(R.string.console_gen_title), getString(R.string.console_gen_description), ColorUtility.parseColorFromId(getActivity(), R.color.primary_dark), ColorUtility.parseColorFromId(getActivity(), R.color.primary_dark), true, false);
        _cardUI.addCard(consoleGen);

        ConsoleCard console = new ConsoleCard(this, getString(R.string.console_task_title), getString(R.string.console_task_description), ColorUtility.parseColorFromId(getActivity(), R.color.primary_dark), ColorUtility.parseColorFromId(getActivity(), R.color.primary_dark), true, false);
        _cardUI.addCard(console);

        _cardUI.refresh();

        return root;
    }

    public void redrawFragment() {
        _cardUI.refresh();
    }

}
