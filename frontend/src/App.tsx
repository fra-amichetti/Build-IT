import React, { useState, useEffect } from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { ViewAutenticazione } from './auth/ViewAutenticazione';
import { ViewRegistrazione } from './auth/ViewRegistrazione';
import { HomeAmministratore } from './screens/HomeAmministratore';
import { HomeListaCantieri } from './screens/HomeListaCantieri';
import { HomeCantiere } from './screens/HomeCantiere';
import { HomeFase } from './screens/HomeFase';
import { HomeDocumentiTecnici } from './screens/HomeDocumentiTecnici';
import { HomeDocumentiContabili } from './screens/HomeDocumentiContabili';
import { HomeGestioneDipendenti } from './screens/HomeGestioneDipendenti';
import { HomeGestioneSquadre } from './screens/HomeGestioneSquadre';
import { ViewMostraStatistiche } from './screens/ViewMostraStatistiche';
import { HomeCliente } from './screens/HomeCliente';
import { ViewAggiungiCantiere } from './forms/ViewAggiungiCantiere';
import { ViewModificaCantiere } from './forms/ViewModificaCantiere';
import { ViewTerminaCantiere } from './forms/ViewTerminaCantiere';
import { ViewAggiungiFaseCantiere } from './forms/ViewAggiungiFaseCantiere';
import { ViewModificaFaseCantiere } from './forms/ViewModificaFaseCantiere';
import { ViewTerminaFaseCantiere } from './forms/ViewTerminaFaseCantiere';
import { ViewAggiungiDocumentoTecnico } from './forms/ViewAggiungiDocumentoTecnico';
import { ViewAggiungiDocumentoContabile } from './forms/ViewAggiungiDocumentoContabile';
import { ConstructionSite, WorkPhase } from './types';
import { terminaCantiere, getDettagliCantiere } from './services/api';

type Screen =
  | 'auth'
  | 'register'
  | 'homeAdmin'
  | 'homeCliente'
  | 'cantieri'
  | 'cantiere'
  | 'aggiungiCantiere'
  | 'modificaCantiere'
  | 'terminaCantiere'
  | 'fase'
  | 'aggiungiFase'
  | 'modificaFase'
  | 'documentiTecnici'
  | 'documentiContabili'
  | 'aggiungiDocumentoTecnico'
  | 'aggiungiDocumentoContabile'
  | 'dipendenti'
  | 'squadre'
  | 'statistiche';

function AppContent() {
  const { currentUser } = useApp();
  const [currentScreen, setCurrentScreen] = useState<Screen>('auth');
  const [selectedSite, setSelectedSite] = useState<ConstructionSite | null>(null);
  const [selectedPhase, setSelectedPhase] = useState<WorkPhase | null>(null);
  const [showPhaseCompleteDialog, setShowPhaseCompleteDialog] = useState(false);
const [loggedUser, setLoggedUser] = useState<any>(null);
  const handleLoginSuccess = (role: string, user: any) => {
  setLoggedUser(user);
  if (role === 'Amministratore') {
    setCurrentScreen('homeAdmin');
  } else if (role === 'Dipendente') {
    setCurrentScreen('cantieri');
  } else if (role === 'Cliente') {
    setCurrentScreen('homeCliente');
  }
};

  // Redirect to auth when user logs out
  useEffect(() => {
    if (!currentUser) {
      setCurrentScreen('auth');
      setSelectedSite(null);
      setSelectedPhase(null);
    }
  }, [currentUser]);

  const handleSelectSite = (site: ConstructionSite) => {
    setSelectedSite(site);
    setCurrentScreen('cantiere');
  };

  const handleSelectPhase = (phase: WorkPhase) => {
    setSelectedPhase(phase);
    setCurrentScreen('fase');
  };

  // Per sviluppo: permette accesso senza login
  // Cambia a false per richiedere autenticazione
  const DEV_MODE = true;

  const isAdmin = loggedUser?.ruolo === 'AMMINISTRATORE';
const isDipendente = loggedUser?.ruolo === 'DIPENDENTE';
const canEdit = isAdmin || isDipendente;
const isReadOnly = loggedUser?.ruolo === 'CLIENTE';
  // Render current screen
  const renderScreen = () => {
    switch (currentScreen) {
      case 'auth':
        return (
          <ViewAutenticazione
            onSuccess={handleLoginSuccess}
            onRegister={() => setCurrentScreen('register')}
          />
        );

      case 'register':
        return <ViewRegistrazione onBack={() => setCurrentScreen('auth')} />;

    case 'homeAdmin':
  return (
    <HomeAmministratore
      onNavigate={() => setCurrentScreen('cantieri')}
      nomeUtente={loggedUser?.nome}
    />
  );

      case 'homeCliente':
        return (
          <HomeCliente
            onViewSites={() => setCurrentScreen('cantieri')}
          />
        );

      case 'cantieri':
  return (
    <HomeListaCantieri
     onBack={
  loggedUser?.ruolo === 'DIPENDENTE'
    ? undefined
    : loggedUser?.ruolo === 'CLIENTE'
    ? () => setCurrentScreen('homeCliente')
    : () => setCurrentScreen('homeAdmin')
}
      onSelectSite={handleSelectSite}
      onAddSite={canEdit ? () => setCurrentScreen('aggiungiCantiere') : undefined}
      readOnly={isReadOnly}
      clientEmail={currentUser?.role === 'Cliente' ? currentUser.email : undefined}
    />
  );

      case 'cantiere':
        if (!selectedSite) return null;
        return (
          <HomeCantiere
            site={selectedSite}
            onBack={() => setCurrentScreen('cantieri')}
            onEditSite={canEdit && selectedSite.stato !== 'Terminato' ? () => setCurrentScreen('modificaCantiere') : undefined}
         onCloseSite={canEdit && selectedSite.stato !== 'Terminato' ? async () => {
  const confermato = window.confirm(`Sei sicuro di voler terminare "${selectedSite.nome}"? La data di fine sarà impostata ad oggi. Operazione irreversibile.`);
  if (confermato) {
    try {
      await terminaCantiere(Number(selectedSite.id));
      setCurrentScreen('cantieri');
    } catch (err: any) {
      alert(err.message);
    }
  }
} : undefined}   onAddPhase={canEdit && selectedSite.stato !== 'Terminato' ? () => setCurrentScreen('aggiungiFase') : undefined}
            onSelectPhase={handleSelectPhase}
            onOpenTechnicalDocs={() => setCurrentScreen('documentiTecnici')}
            onOpenAccountingDocs={() => setCurrentScreen('documentiContabili')}
            readOnly={isReadOnly || selectedSite.stato === 'Terminato'}
          />
        );

      case 'aggiungiCantiere':
        return (
          <ViewAggiungiCantiere
            onBack={() => setCurrentScreen('cantieri')}
            onSuccess={() => setCurrentScreen('cantieri')}
          />
        );

      case 'modificaCantiere':
  if (!selectedSite) return null;
  return (
    <ViewModificaCantiere
      site={selectedSite}
      onBack={() => setCurrentScreen('cantiere')}
      onSuccess={async () => {
        const aggiornato = await getDettagliCantiere(Number(selectedSite.id));
        setSelectedSite(aggiornato);
        setCurrentScreen('cantiere');
      }}
    />
  );

      case 'terminaCantiere':
        if (!selectedSite) return null;
        return (
          <ViewTerminaCantiere
            site={selectedSite}
            onBack={() => setCurrentScreen('cantiere')}
            onSuccess={() => {
              setSelectedSite(null);
              setCurrentScreen('cantieri');
            }}
          />
        );

      case 'fase':
        if (!selectedPhase || !selectedSite) return null;
        return (
          <HomeFase
            phase={selectedPhase}
            site={selectedSite}
            onBack={() => setCurrentScreen('cantiere')}
            onEditPhase={canEdit && selectedPhase.stato !== 'Completata' ? () => setCurrentScreen('modificaFase') : undefined}
            onCompletePhase={canEdit && selectedPhase.stato !== 'Completata' ? () => setShowPhaseCompleteDialog(true) : undefined}
            readOnly={isReadOnly || selectedPhase.stato === 'Completata' || selectedSite.stato === 'Terminato'}
          />
        );

      case 'aggiungiFase':
        if (!selectedSite) return null;
        return (
          <ViewAggiungiFaseCantiere
            site={selectedSite}
            onBack={() => setCurrentScreen('cantiere')}
            onSuccess={() => setCurrentScreen('cantiere')}
          />
        );

      case 'modificaFase':
        if (!selectedPhase || !selectedSite) return null;
        return (
          <ViewModificaFaseCantiere
            phase={selectedPhase}
            site={selectedSite}
            onBack={() => setCurrentScreen('fase')}
            onSuccess={() => setCurrentScreen('fase')}
          />
        );

      case 'documentiTecnici':
        if (!selectedSite) return null;
        return (
          <HomeDocumentiTecnici
            site={selectedSite}
            onBack={() => setCurrentScreen('cantiere')}
            onAddDocument={canEdit && selectedSite.stato !== 'Terminato' ? () => setCurrentScreen('aggiungiDocumentoTecnico') : undefined}
            readOnly={isReadOnly || selectedSite.stato === 'Terminato'}
          />
        );

      case 'documentiContabili':
        if (!selectedSite) return null;
        return (
          <HomeDocumentiContabili
            site={selectedSite}
            onBack={() => setCurrentScreen('cantiere')}
            onAddDocument={canEdit && selectedSite.stato !== 'Terminato' ? () => setCurrentScreen('aggiungiDocumentoContabile') : undefined}
            readOnly={isReadOnly || selectedSite.stato === 'Terminato'}
          />
        );

      case 'aggiungiDocumentoTecnico':
        if (!selectedSite) return null;
        return (
          <ViewAggiungiDocumentoTecnico
            site={selectedSite}
            onBack={() => setCurrentScreen('documentiTecnici')}
            onSuccess={() => setCurrentScreen('documentiTecnici')}
          />
        );

      case 'aggiungiDocumentoContabile':
        if (!selectedSite) return null;
        return (
          <ViewAggiungiDocumentoContabile
            site={selectedSite}
            onBack={() => setCurrentScreen('documentiContabili')}
            onSuccess={() => setCurrentScreen('documentiContabili')}
          />
        );

      case 'dipendenti':
        return (
          <HomeGestioneDipendenti
            onBack={() => setCurrentScreen('homeAdmin')}
          />
        );

      case 'squadre':
        return (
          <HomeGestioneSquadre
            onBack={() => setCurrentScreen('homeAdmin')}
          />
        );

      case 'statistiche':
        return (
          <ViewMostraStatistiche
            onBack={() => setCurrentScreen('homeAdmin')}
          />
        );

      default:
        return <ViewAutenticazione onSuccess={handleLoginSuccess} onRegister={() => setCurrentScreen('register')} />;
    }
  };

  return (
    <>
      {renderScreen()}
      {selectedPhase && selectedSite && showPhaseCompleteDialog && (
        <ViewTerminaFaseCantiere
          phase={selectedPhase}
          site={selectedSite}
          isOpen={showPhaseCompleteDialog}
          onCancel={() => setShowPhaseCompleteDialog(false)}
          onSuccess={() => {
            setShowPhaseCompleteDialog(false);
            setCurrentScreen('cantiere');
          }}
        />
      )}
    </>
  );
}

function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}

export default App;
