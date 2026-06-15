import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { User, ConstructionSite, WorkPhase, Team, TechnicalDocument, AccountingDocument } from '../types';
import {
  mockUsers,
  mockConstructionSites,
  mockWorkPhases,
  mockTeams,
  mockTechnicalDocuments,
  mockAccountingDocuments,
} from '../data/mockData';

interface AppContextType {
  // Auth
  currentUser: User | null;
  login: (email: string, password: string) => { success: boolean; error?: string; user?: User };
  logout: () => void;
  register: (nome: string, cognome: string, email: string, password: string) => { success: boolean; error?: string };

  // Users
  users: User[];
  addEmployee: (nome: string, cognome: string, email: string, password: string) => { success: boolean; error?: string };
  deleteEmployee: (userId: string) => void;

  // Construction Sites
  constructionSites: ConstructionSite[];
  addConstructionSite: (site: Omit<ConstructionSite, 'id'>) => ConstructionSite;
  updateConstructionSite: (siteId: string, updates: Partial<ConstructionSite>) => void;
  closeConstructionSite: (siteId: string, dataConsegnaEffettiva: string) => boolean;

  // Work Phases
  workPhases: WorkPhase[];
  addWorkPhase: (phase: Omit<WorkPhase, 'id'>) => { success: boolean; phase?: WorkPhase; error?: string };
  updateWorkPhase: (phaseId: string, updates: Partial<WorkPhase>) => { success: boolean; error?: string };
  completeWorkPhase: (phaseId: string) => void;

  // Teams
  teams: Team[];
  addTeam: (team: Omit<Team, 'id'>) => { success: boolean; error?: string };
  deleteTeam: (teamId: string) => { success: boolean; error?: string };

  // Technical Documents
  technicalDocuments: TechnicalDocument[];
  addTechnicalDocument: (doc: Omit<TechnicalDocument, 'id'>) => TechnicalDocument;
  deleteTechnicalDocument: (docId: string) => void;

  // Accounting Documents
  accountingDocuments: AccountingDocument[];
  addAccountingDocument: (doc: Omit<AccountingDocument, 'id'>) => AccountingDocument;
  deleteAccountingDocument: (docId: string) => void;
  markInvoicePaid: (docId: string) => void;

  // Helpers
  getTeamById: (teamId: string) => Team | undefined;
  getPhasesBySite: (siteId: string) => WorkPhase[];
  getSiteById: (siteId: string) => ConstructionSite | undefined;
  getPhaseById: (phaseId: string) => WorkPhase | undefined;
  checkTeamOverlap: (teamId: string, startDate: string, endDate: string, excludePhaseId?: string) => boolean;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [users, setUsers] = useState<User[]>(mockUsers);
  const [constructionSites, setConstructionSites] = useState<ConstructionSite[]>(mockConstructionSites);
  const [workPhases, setWorkPhases] = useState<WorkPhase[]>(mockWorkPhases);
  const [teams, setTeams] = useState<Team[]>(mockTeams);
  const [technicalDocuments, setTechnicalDocuments] = useState<TechnicalDocument[]>(mockTechnicalDocuments);
  const [accountingDocuments, setAccountingDocuments] = useState<AccountingDocument[]>(mockAccountingDocuments);

  // Deadline check - runs on mount and every hour
  const checkDeadlines = useCallback(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    setConstructionSites((prevSites) =>
      prevSites.map((site) => {
        if (site.stato === 'In Corso') {
          const estimatedEnd = new Date(site.dataFinePrevista);
          estimatedEnd.setHours(0, 0, 0, 0);

          if (today > estimatedEnd) {
            return { ...site, stato: 'In Ritardo' as const };
          }
        }
        return site;
      })
    );

    setWorkPhases((prevPhases) =>
      prevPhases.map((phase) => {
        if (phase.stato === 'In Corso') {
          const estimatedEnd = new Date(phase.dataFinePrevista);
          estimatedEnd.setHours(0, 0, 0, 0);

          if (today > estimatedEnd) {
            return { ...phase, stato: 'In Ritardo' as const };
          }
        }
        return phase;
      })
    );
  }, []);

  useEffect(() => {
    checkDeadlines();
    const interval = setInterval(checkDeadlines, 3600000); // Check every hour
    return () => clearInterval(interval);
  }, [checkDeadlines]);

  // Auth
  const login = (email: string, password: string): { success: boolean; error?: string; user?: User } => {
    const user = users.find((u) => u.email.toLowerCase() === email.toLowerCase() && u.password === password);

    if (!user) {
      const existsButWrongPassword = users.find((u) => u.email.toLowerCase() === email.toLowerCase());
      if (existsButWrongPassword) {
        return { success: false, error: 'Password non corretta' };
      }
      return { success: false, error: 'Utente non registrato' };
    }

    setCurrentUser(user);
    return { success: true, user };
  };

  const logout = () => {
    setCurrentUser(null);
  };

  const register = (nome: string, cognome: string, email: string, password: string) => {
    const exists = users.find((u) => u.email.toLowerCase() === email.toLowerCase());
    if (exists) {
      return { success: false, error: 'Email già registrata' };
    }

    const newUser: User = {
      id: `user-${Date.now()}`,
      email,
      password,
      nome,
      cognome,
      role: 'Cliente',
    };

    setUsers([...users, newUser]);
    return { success: true };
  };

  // Users
  const addEmployee = (nome: string, cognome: string, email: string, password: string) => {
    const exists = users.find((u) => u.email.toLowerCase() === email.toLowerCase());
    if (exists) {
      return { success: false, error: 'Email già registrata' };
    }

    const newUser: User = {
      id: `user-${Date.now()}`,
      email,
      password,
      nome,
      cognome,
      role: 'Dipendente',
    };

    setUsers([...users, newUser]);
    return { success: true };
  };

  const deleteEmployee = (userId: string) => {
    setUsers(users.filter((u) => u.id !== userId));
  };

  // Construction Sites
  const addConstructionSite = (site: Omit<ConstructionSite, 'id'>) => {
    const newSite: ConstructionSite = {
      ...site,
      id: `site-${Date.now()}`,
    };
    setConstructionSites([...constructionSites, newSite]);
    return newSite;
  };

  const updateConstructionSite = (siteId: string, updates: Partial<ConstructionSite>) => {
    setConstructionSites(
      constructionSites.map((site) =>
        site.id === siteId ? { ...site, ...updates } : site
      )
    );
  };

  const closeConstructionSite = (siteId: string, dataConsegnaEffettiva: string) => {
    const site = constructionSites.find((s) => s.id === siteId);
    if (!site) return false;

    const startDate = new Date(site.dataInizioPrevista);
    const deliveryDate = new Date(dataConsegnaEffettiva);

    if (deliveryDate <= startDate) {
      return false;
    }

    setConstructionSites(
      constructionSites.map((s) =>
        s.id === siteId
          ? { ...s, stato: 'Terminato' as const, dataFineEffettiva : dataConsegnaEffettiva }
          : s
      )
    );
    return true;
  };

  // Work Phases
  const checkTeamOverlap = useCallback(
    (teamId: string, startDate: string, endDate: string, excludePhaseId?: string): boolean => {
      const start = new Date(startDate);
      const end = new Date(endDate);

      return workPhases.some((phase) => {
        if (phase.id === excludePhaseId) return false;
        if (phase.squadraId !== teamId) return false;
        if (phase.stato === 'Completata') return false;

        const phaseStart = new Date(phase.dataInizio);
        const phaseEnd = new Date(phase.dataFineEffettiva || phase.dataFinePrevista);

        return start <= phaseEnd && end >= phaseStart;
      });
    },
    [workPhases]
  );

  const addWorkPhase = (phase: Omit<WorkPhase, 'id'>) => {
    if (checkTeamOverlap(phase.squadraId, phase.dataInizio, phase.dataFinePrevista)) {
      return { success: false, error: 'Squadra già impegnata in questo periodo' };
    }

    const newPhase: WorkPhase = {
      ...phase,
      id: `phase-${Date.now()}`,
    };
    setWorkPhases([...workPhases, newPhase]);
    return { success: true, phase: newPhase };
  };

  const updateWorkPhase = (phaseId: string, updates: Partial<WorkPhase>) => {
    const phase = workPhases.find((p) => p.id === phaseId);
    if (!phase) {
      return { success: false, error: 'Fase non trovata' };
    }

    const teamId = updates.squadraId || phase.squadraId;
    const startDate = updates.dataInizio || phase.dataInizio;
    const endDate = updates.dataFinePrevista || phase.dataFinePrevista;

    if (checkTeamOverlap(teamId, startDate, endDate, phaseId)) {
      return { success: false, error: 'Squadra già impegnata in questo periodo' };
    }

    setWorkPhases(
      workPhases.map((p) =>
        p.id === phaseId ? { ...p, ...updates } : p
      )
    );
    return { success: true };
  };

  const completeWorkPhase = (phaseId: string) => {
    const today = new Date().toISOString().split('T')[0];
    setWorkPhases(
      workPhases.map((phase) =>
        phase.id === phaseId
          ? { ...phase, stato: 'Completata' as const, dataFineEffettiva: today }
          : phase
      )
    );
  };

  // Teams
  const addTeam = (team: Omit<Team, 'id'>) => {
    const exists = teams.find((t) => t.nome.toLowerCase() === team.nome.toLowerCase());
    if (exists) {
      return { success: false, error: 'Nome squadra già esistente' };
    }

    const newTeam: Team = {
      ...team,
      id: `team-${Date.now()}`,
    };
    setTeams([...teams, newTeam]);
    return { success: true };
  };

  const deleteTeam = (teamId: string) => {
    const hasActivePhases = workPhases.some(
      (phase) => phase.squadraId === teamId && (phase.stato === 'In Corso' || phase.stato === 'Pianificata')
    );

    if (hasActivePhases) {
      return { success: false, error: 'Squadra impegnata in fasi attive' };
    }

    setTeams(teams.filter((t) => t.id !== teamId));
    return { success: true };
  };

  // Technical Documents
  const addTechnicalDocument = (doc: Omit<TechnicalDocument, 'id'>) => {
    const newDoc: TechnicalDocument = {
      ...doc,
      id: `techdoc-${Date.now()}`,
    };
    setTechnicalDocuments([...technicalDocuments, newDoc]);
    return newDoc;
  };

  const deleteTechnicalDocument = (docId: string) => {
    setTechnicalDocuments(technicalDocuments.filter((d) => d.id !== docId));
  };

  // Accounting Documents
  const addAccountingDocument = (doc: Omit<AccountingDocument, 'id'>) => {
    const newDoc: AccountingDocument = {
      ...doc,
      id: `accdoc-${Date.now()}`,
    };
    setAccountingDocuments([...accountingDocuments, newDoc]);
    return newDoc;
  };

  const deleteAccountingDocument = (docId: string) => {
    setAccountingDocuments(accountingDocuments.filter((d) => d.id !== docId));
  };

  const markInvoicePaid = (docId: string) => {
    setAccountingDocuments(
      accountingDocuments.map((doc) =>
        doc.id === docId ? { ...doc, statoPagamento: 'Saldato' as const } : doc
      )
    );
  };

  // Helpers
  const getTeamById = (teamId: string) => teams.find((t) => t.id === teamId);
  const getPhasesBySite = (siteId: string) => workPhases.filter((p) => p.cantiereId === siteId);
  const getSiteById = (siteId: string) => constructionSites.find((s) => s.id === siteId);
  const getPhaseById = (phaseId: string) => workPhases.find((p) => p.id === phaseId);

  const value: AppContextType = {
    currentUser,
    login,
    logout,
    register,
    users,
    addEmployee,
    deleteEmployee,
    constructionSites,
    addConstructionSite,
    updateConstructionSite,
    closeConstructionSite,
    workPhases,
    addWorkPhase,
    updateWorkPhase,
    completeWorkPhase,
    teams,
    addTeam,
    deleteTeam,
    technicalDocuments,
    addTechnicalDocument,
    deleteTechnicalDocument,
    accountingDocuments,
    addAccountingDocument,
    deleteAccountingDocument,
    markInvoicePaid,
    getTeamById,
    getPhasesBySite,
    getSiteById,
    getPhaseById,
    checkTeamOverlap,
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
}
