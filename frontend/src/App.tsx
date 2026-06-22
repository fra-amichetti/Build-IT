import React, { useState, useEffect } from 'react';
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
import { terminaCantiere, getDettagliCantiere, getDettagliFase } from './services/api';

const SESSION_KEY = 'buildit_session';

type SessionData = {
  user: any;
  screen: Screen;
  siteId: number | null;
  phaseId: number | null;
};

function homeForRole(ruolo: string): Screen {
  if (ruolo === 'AMMINISTRATORE') return 'homeAdmin';
  if (ruolo === 'DIPENDENTE') return 'cantieri';
  return 'homeCliente';
}

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

  const [currentScreen, setCurrentScreen] = useState<Screen>('auth');
  const [selectedSite, setSelectedSite] = useState<ConstructionSite | null>(null);
  const [selectedPhase, setSelectedPhase] = useState<WorkPhase | null>(null);
  const [showPhaseCompleteDialog, setShowPhaseCompleteDialog] = useState(false);
  const [loggedUser, setLoggedUser] = useState<any>(null);

  // ── Ripristino sessione al refresh ────────────────────────────────────────
  useEffect(() => {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) return;
    try {
      const { user, screen, siteId, phaseId }: SessionData = JSON.parse(raw);
      setLoggedUser(user);

      const NEEDS_SITE: Screen[] = [
        'cantiere', 'modificaCantiere', 'terminaCantiere', 'aggiungiFase',
        'documentiTecnici', 'documentiContabili',
        'aggiungiDocumentoTecnico', 'aggiungiDocumentoContabile',
      ];
      const NEEDS_PHASE: Screen[] = ['fase', 'modificaFase'];

      if (siteId && (NEEDS_SITE.includes(screen) || NEEDS_PHASE.includes(screen))) {
        getDettagliCantiere(siteId)
          .then(site => {
            setSelectedSite(site);
            if (phaseId && NEEDS_PHASE.includes(screen)) {
              return getDettagliFase(phaseId).then(phase => {
                setSelectedPhase(phase);
                setCurrentScreen(screen);
              });
            }
            setCurrentScreen(screen);
          })
          .catch(() => setCurrentScreen(homeForRole(user.ruolo)));
      } else {
        setCurrentScreen(screen ?? homeForRole(user.ruolo));
      }
    } catch {
      sessionStorage.removeItem(SESSION_KEY);
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ── Salvataggio sessione ad ogni cambio di stato ──────────────────────────
  useEffect(() => {
    if (!loggedUser) return;
    const session: SessionData = {
      user: loggedUser,
      screen: currentScreen,
      siteId: selectedSite ? Number(selectedSite.id) : null,
      phaseId: selectedPhase ? Number(selectedPhase.id) : null,
    };
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  }, [loggedUser, currentScreen, selectedSite, selectedPhase]);

  // ── Logout ────────────────────────────────────────────────────────────────
  const handleLogout = () => {
    setLoggedUser(null);
    setSelectedSite(null);
    setSelectedPhase(null);
    setCurrentScreen('auth');
  };

  useEffect(() => {
    window.addEventListener('buildit_logout', handleLogout);
    return () => window.removeEventListener('buildit_logout', handleLogout);
  });

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

  const handleSelectSite = (site: ConstructionSite) => {
    setSelectedSite(site);
    setCurrentScreen('cantiere');
  };

  const handleSelectPhase = (phase: WorkPhase) => {
    setSelectedPhase(phase);
    setCurrentScreen('fase');
  };

  const isAdmin = loggedUser?.ruolo === 'AMMINISTRATORE';
  const isDipendente = loggedUser?.ruolo === 'DIPENDENTE';
  const canEdit = isAdmin || isDipendente;
  const isReadOnly = loggedUser?.ruolo === 'CLIENTE';

  useEffect(() => {
    const handleHomeEvent = () => {
      if (isAdmin) setCurrentScreen('homeAdmin');
      else if (isDipendente) setCurrentScreen('cantieri');
      else if (isReadOnly) setCurrentScreen('homeCliente');
    };

    window.addEventListener('tornaAllaHome', handleHomeEvent);
    return () => {
      window.removeEventListener('tornaAllaHome', handleHomeEvent);
    };
  }, [isAdmin, isDipendente, isReadOnly]);

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
            utente={loggedUser}
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
            clientEmail={loggedUser?.ruolo === 'CLIENTE' ? loggedUser.email : undefined}
          />
        );

      case 'cantiere':
        if (!selectedSite) return null;
        return (
          <HomeCantiere
            site={selectedSite}
            onBack={() => setCurrentScreen('cantieri')}
            onEditSite={canEdit && selectedSite.stato !== 'TERMINATO' ? () => setCurrentScreen('modificaCantiere') : undefined}
            onCloseSite={canEdit && selectedSite.stato !== 'TERMINATO' ? async () => {
              const confermato = window.confirm(`Sei sicuro di voler terminare "${selectedSite.nome}"? La data di fine sarà impostata ad oggi. Operazione irreversibile.`);
              if (confermato) {
                try {
                  await terminaCantiere(Number(selectedSite.id));
                  setCurrentScreen('cantieri');
                } catch (err: any) {
                  alert(err.message);
                }
              }
            } : undefined}
            onAddPhase={canEdit && selectedSite.stato !== 'TERMINATO' ? () => setCurrentScreen('aggiungiFase') : undefined}
            onSelectPhase={handleSelectPhase}
            onOpenTechnicalDocs={() => setCurrentScreen('documentiTecnici')}
            onOpenAccountingDocs={() => setCurrentScreen('documentiContabili')}
            readOnly={isReadOnly || selectedSite.stato === 'TERMINATO'}
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
            onEditPhase={canEdit && selectedPhase.stato !== 'TERMINATA' ? () => setCurrentScreen('modificaFase') : undefined}
            onCompletePhase={canEdit && selectedPhase.stato !== 'TERMINATA' ? () => setShowPhaseCompleteDialog(true) : undefined}
            readOnly={isReadOnly}
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
            onSuccess={async () => {
              const aggiornata = await getDettagliFase(Number(selectedPhase.id));
              setSelectedPhase(aggiornata);
              setCurrentScreen('fase');
            }}
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
    <AppContent />
  );
}

export default App;
