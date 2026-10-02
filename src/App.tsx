import React from 'react';
import ScreenTranslatorView from './components/ScreenTranslatorView';

export default function App() {
  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-200 p-4 sm:p-8 font-sans selection:bg-blue-500/30 selection:text-white">
      <div className="max-w-5xl mx-auto space-y-6">
        <ScreenTranslatorView />
      </div>
    </div>
  );
}
