"use client";

import { useState, useEffect, useRef } from "react";

export default function Home() {
  // Authentification State
  const [token, setToken] = useState("");
  const [userRoles, setUserRoles] = useState([]);
  const [username, setUsername] = useState("hr");
  const [password, setPassword] = useState("hr123");
  const [loginError, setLoginError] = useState("");
  const [loadingLogin, setLoadingLogin] = useState(false);

  // Navigation State
  const [activeTab, setActiveTab] = useState("dashboard");

  // Dashboard State
  const [kpis, setKpis] = useState(null);
  const [loadingKpis, setLoadingKpis] = useState(false);

  // ML Predictor State
  const [age, setAge] = useState(30);
  const [monthlyIncome, setMonthlyIncome] = useState(12000);
  const [yearsAtCompany, setYearsAtCompany] = useState(3);
  const [jobSatisfaction, setJobSatisfaction] = useState(3);
  const [workLifeBalance, setWorkLifeBalance] = useState(3);
  const [overtime, setOvertime] = useState(0);
  const [numPromotions, setNumPromotions] = useState(0);
  
  const [prediction, setPrediction] = useState(null);
  const [loadingPredict, setLoadingPredict] = useState(false);

  // RAG Document Upload State
  const [employees, setEmployees] = useState([]);
  const [uploadEmployeeId, setUploadEmployeeId] = useState("");
  const [uploadFileType, setUploadFileType] = useState("CONTRACT");
  const [uploadFile, setUploadFile] = useState(null);
  const [uploadDescription, setUploadDescription] = useState("");
  const [uploadStatus, setUploadStatus] = useState("");
  const [loadingUpload, setLoadingUpload] = useState(false);

  // Expense Anomaly Audit State (Axe C)
  const [expenseAmount, setExpenseAmount] = useState(150);
  const [expenseCategory, setExpenseCategory] = useState("MEAL");
  const [expenseDay, setExpenseDay] = useState("MONDAY");
  const [expenseAnalysis, setExpenseAnalysis] = useState(null);
  const [loadingExpense, setLoadingExpense] = useState(false);

  // Chatbot State
  const [query, setQuery] = useState("");
  const [chatHistory, setChatHistory] = useState([]);
  const [loadingChat, setLoadingChat] = useState(false);
  const chatEndRef = useRef(null);

  // États d'administration RH (Intégration complète de la collection Postman)
  const [adminEmployees, setAdminEmployees] = useState([]);
  const [adminDepartments, setAdminDepartments] = useState([]);
  const [adminLeaves, setAdminLeaves] = useState([]);
  const [leaveSearchQuery, setLeaveSearchQuery] = useState("");
  const [leaveCurrentPage, setLeaveCurrentPage] = useState(1);
  const [currentEmployee, setCurrentEmployee] = useState(null);
  
  // Formulaire Employé
  const [empFirstName, setEmpFirstName] = useState("");
  const [empLastName, setEmpLastName] = useState("");
  const [empEmail, setEmpEmail] = useState("");
  const [empPhone, setEmpPhone] = useState("+212612345678");
  const [empDOB, setEmpDOB] = useState("1995-01-01");
  const [empGender, setEmpGender] = useState("MALE");
  const [empCIN, setEmpCIN] = useState("");
  const [empNationality, setEmpNationality] = useState("Marocaine");
  const [empAddress, setEmpAddress] = useState("");
  const [empCity, setEmpCity] = useState("Casablanca");
  const [empRIB, setEmpRIB] = useState("");
  const [empCNSS, setEmpCNSS] = useState("");
  
  // Formulaire Département
  const [deptName, setDeptName] = useState("");
  const [deptDescription, setDeptDescription] = useState("");
  const [deptBudget, setDeptBudget] = useState(100000);
  
  // Formulaire Check-In
  const [checkInEmpId, setCheckInEmpId] = useState("");
  const [checkInNotes, setCheckInNotes] = useState("Arrivée au bureau");
  
  // Formulaire Bulletin de paie
  const [payrollEmpId, setPayrollEmpId] = useState("");
  const [payrollMonth, setPayrollMonth] = useState("2026-07-01");
  const [payrollBonus, setPayrollBonus] = useState(0);
  const [payrollOvertime, setPayrollOvertime] = useState(0);
  const [payrollDeductions, setPayrollDeductions] = useState(0);
  
  // Formulaire Demande Congé
  const [leaveEmpId, setLeaveEmpId] = useState("");
  const [leaveStartDate, setLeaveStartDate] = useState("2026-07-20");
  const [leaveEndDate, setLeaveEndDate] = useState("2026-07-25");
  const [leaveReason, setLeaveReason] = useState("Vacances");
  const [leaveType, setLeaveType] = useState("ANNUAL");

  const [adminStatus, setAdminStatus] = useState("");
  const [quickCheckInStatus, setQuickCheckInStatus] = useState("");
  const [adminSubTab, setAdminSubTab] = useState("employees");

  // Dérivations de rôles
  const isHrOrAdmin = userRoles.includes("ROLE_ADMIN") || userRoles.includes("ROLE_HR_MANAGER");
  const isEmployeeOnly = userRoles.includes("ROLE_EMPLOYEE") && 
                         !userRoles.includes("ROLE_ADMIN") && 
                         !userRoles.includes("ROLE_HR_MANAGER") && 
                         !userRoles.includes("ROLE_MANAGER");

  const formatRoleDisplay = () => {
    if (userRoles.includes("ROLE_ADMIN")) return "Administrateur";
    if (userRoles.includes("ROLE_HR_MANAGER")) return "Responsable RH";
    if (userRoles.includes("ROLE_MANAGER")) return "Manager";
    return "Employé";
  };

  const fetchCurrentEmployee = async () => {
    if (!token) return;
    try {
      let searchKeyword = username;
      if (username === "employee") {
        searchKeyword = "mohamed"; // pour pointer sur Mohamed El Alami
      } else if (username === "hr") {
        searchKeyword = "hr";
      } else if (username === "manager") {
        searchKeyword = "manager";
      }
      
      const searchRes = await fetch(`/api/proxy/employees?keyword=${encodeURIComponent(searchKeyword)}&size=1`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (searchRes.ok) {
        const data = await searchRes.json();
        const empList = data.content || data || [];
        if (empList.length > 0) {
          setCurrentEmployee(empList[0]);
          setLeaveEmpId(empList[0].id);
        }
      } else if (searchRes.status === 401 || searchRes.status === 403) {
        handleLogout();
      }
    } catch (e) {
      console.error("Error loading current employee:", e);
    }
  };

  const handleQuickCheckIn = async () => {
    setQuickCheckInStatus("⏳ Recherche de votre profil...");
    try {
      // Déterminer le mot-clé de recherche pour l'employé connecté
      let searchKeyword = username;
      if (username === "employee") {
        searchKeyword = "mohamed"; // pour pointer sur Mohamed El Alami
      } else if (username === "hr") {
        searchKeyword = "hr";
      } else if (username === "manager") {
        searchKeyword = "manager";
      }
      
      // 1. Rechercher l'employé en BDD par mot-clé
      const searchRes = await fetch(`/api/proxy/employees?keyword=${encodeURIComponent(searchKeyword)}&size=1`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      
      if (!searchRes.ok) {
        setQuickCheckInStatus("❌ Impossible de trouver un profil employé associé à ce compte.");
        setTimeout(() => setQuickCheckInStatus(""), 4000);
        return;
      }
      
      const searchData = await searchRes.json();
      const empList = searchData.content || searchData || [];
      if (empList.length === 0) {
        setQuickCheckInStatus("❌ Aucun profil collaborateur correspondant à votre identifiant.");
        setTimeout(() => setQuickCheckInStatus(""), 4000);
        return;
      }
      
      const targetEmpId = empList[0].id;
      const targetEmpName = `${empList[0].firstName} ${empList[0].lastName}`;
      
      // 2. Faire le check-in
      setQuickCheckInStatus(`⏳ Enregistrement du pointage pour ${targetEmpName}...`);
      const checkInRes = await fetch(`/api/proxy/attendance/check-in?employeeId=${targetEmpId}&notes=${encodeURIComponent("Pointage rapide depuis le Header")}`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}` }
      });
      
      if (checkInRes.ok) {
        setQuickCheckInStatus(`✅ Pointage d'entrée enregistré pour ${targetEmpName} !`);
        // Rafraîchir les KPIs pour mettre à jour les "Présents du jour"
        fetchKpis();
      } else {
        const err = await checkInRes.json();
        setQuickCheckInStatus(`❌ Erreur: ${err.message || 'Déjà pointé aujourd\'hui'}`);
      }
    } catch (err) {
      console.error("Error during quick check-in:", err);
      setQuickCheckInStatus("❌ Erreur de connexion.");
    }
    setTimeout(() => setQuickCheckInStatus(""), 5000);
  };

  // Effet d'initialisation (charger le token et les rôles si présents)
  useEffect(() => {
    const savedToken = localStorage.getItem("hr_jwt_token");
    const savedRoles = localStorage.getItem("hr_user_roles");
    if (savedToken) {
      setToken(savedToken);
      if (savedRoles) {
        try {
          const parsedRoles = JSON.parse(savedRoles);
          setUserRoles(parsedRoles);
          
          // Si l'utilisateur est un simple employé, on force l'onglet chatbot par défaut
          const isEmployeeOnly = parsedRoles.includes("ROLE_EMPLOYEE") && 
                                 !parsedRoles.includes("ROLE_ADMIN") && 
                                 !parsedRoles.includes("ROLE_HR_MANAGER") && 
                                 !parsedRoles.includes("ROLE_MANAGER");
          if (isEmployeeOnly) {
            setActiveTab("chatbot");
          }
        } catch (e) {
          console.error("Error parsing user roles:", e);
        }
      }
    }
  }, []);

  // Défilement automatique du chat
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatHistory]);

  // Charger les KPIs dès que le token ou l'onglet dashboard est actif
  useEffect(() => {
    if (token) {
      fetchCurrentEmployee();
      fetchEmployees();
      if (activeTab === "dashboard") {
        fetchKpis();
      }
    }
  }, [token, activeTab]);

  // Déclencher le chargement des données administratives ou des congés
  useEffect(() => {
    if (token && (activeTab === "admin" || activeTab === "leaves")) {
      fetchAdminData();
    }
  }, [token, activeTab]);

  const fetchAdminData = async () => {
    if (!token) return;
    try {
      // Lister les employés
      const empRes = await fetch("/api/proxy/employees", {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (empRes.ok) {
        const data = await empRes.json();
        const empList = data.content || data || [];
        setAdminEmployees(empList);
        if (empList.length > 0 && !leaveEmpId) {
          setCheckInEmpId(empList[0].id);
          setPayrollEmpId(empList[0].id);
          setLeaveEmpId(empList[0].id);
        }
      } else if (empRes.status === 401 || empRes.status === 403) {
        handleLogout();
        return;
      }
      
      // Lister les départements
      const deptRes = await fetch("/api/proxy/departments", {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (deptRes.ok) {
        const data = await deptRes.json();
        setAdminDepartments(data);
      } else if (deptRes.status === 401 || deptRes.status === 403) {
        handleLogout();
        return;
      }
      
      // Lister les congés
      const leaveRes = await fetch("/api/proxy/leaves", {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (leaveRes.ok) {
        const data = await leaveRes.json();
        setAdminLeaves(data);
      } else if (leaveRes.status === 401 || leaveRes.status === 403) {
        handleLogout();
        return;
      }
    } catch (err) {
      console.error("Error loading admin data:", err);
    }
  };

  const cleanErrorMessage = (msg) => {
    if (!msg) return "Une erreur est survenue.";
    if (typeof msg === "object") return JSON.stringify(msg);
    if (typeof msg === "string" && msg.trim().startsWith("{")) {
      try {
        const parsed = JSON.parse(msg);
        if (parsed.message) {
          return cleanErrorMessage(parsed.message);
        }
      } catch (e) {}
    }
    return msg.replace(/^[A-Z_]+:\s*/, "");
  };

  const handleCreateEmployee = async (e) => {
    e.preventDefault();
    setAdminStatus("⏳ Création du collaborateur...");
    try {
      const res = await fetch("/api/proxy/employees", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          firstName: empFirstName,
          lastName: empLastName,
          email: empEmail,
          phone: empPhone,
          dateOfBirth: empDOB,
          gender: empGender,
          cin: empCIN,
          nationality: empNationality,
          address: empAddress,
          city: empCity,
          rib: empRIB,
          cnss: empCNSS,
          status: "ACTIVE",
          hireDate: new Date().toISOString().split('T')[0],
          maritalStatus: "SINGLE",
          numberOfChildren: 0
        })
      });
      if (res.ok) {
        setAdminStatus("✅ Collaborateur créé avec succès !");
        setEmpFirstName("");
        setEmpLastName("");
        setEmpEmail("");
        setEmpCIN("");
        setEmpAddress("");
        setEmpRIB("");
        setEmpCNSS("");
        fetchAdminData();
      } else {
        const err = await res.json();
        setAdminStatus(`❌ Erreur: ${cleanErrorMessage(err.message || 'Échec de création')}`);
      }
    } catch (err) {
      setAdminStatus("❌ Erreur de connexion.");
    }
  };

  const handleCreateDepartment = async (e) => {
    e.preventDefault();
    setAdminStatus("⏳ Création du département...");
    try {
      const res = await fetch("/api/proxy/departments", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          name: deptName,
          description: deptDescription,
          budget: parseFloat(deptBudget)
        })
      });
      if (res.ok) {
        setAdminStatus("✅ Département créé avec succès !");
        setDeptName("");
        setDeptDescription("");
        fetchAdminData();
      } else {
        const err = await res.json();
        setAdminStatus(`❌ Erreur: ${cleanErrorMessage(err.message)}`);
      }
    } catch (err) {
      setAdminStatus("❌ Erreur de connexion.");
    }
  };

  const handleCheckIn = async (e) => {
    e.preventDefault();
    if (!checkInEmpId) {
      setAdminStatus("⚠️ Sélectionnez un collaborateur.");
      return;
    }
    setAdminStatus("⏳ Pointage en cours...");
    try {
      const res = await fetch(`/api/proxy/attendance/check-in?employeeId=${checkInEmpId}&notes=${encodeURIComponent(checkInNotes)}`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (res.ok) {
        setAdminStatus("✅ Pointage (Check-In) enregistré avec succès !");
      } else {
        const err = await res.json();
        setAdminStatus(`❌ Erreur: ${cleanErrorMessage(err.message)}`);
      }
    } catch (err) {
      setAdminStatus("❌ Erreur de connexion.");
    }
  };

  const handleGeneratePayroll = async (e) => {
    e.preventDefault();
    if (!payrollEmpId) {
      setAdminStatus("⚠️ Sélectionnez un collaborateur.");
      return;
    }
    setAdminStatus("⏳ Génération du bulletin...");
    try {
      const res = await fetch("/api/proxy/payroll", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          employeeId: payrollEmpId,
          payMonth: payrollMonth,
          bonus: parseFloat(payrollBonus),
          overtimeHours: parseFloat(payrollOvertime),
          deductions: parseFloat(payrollDeductions)
        })
      });
      if (res.ok) {
        setAdminStatus("✅ Bulletin de paie généré et enregistré !");
      } else {
        const err = await res.json();
        setAdminStatus(`❌ Erreur: ${cleanErrorMessage(err.message)}`);
      }
    } catch (err) {
      setAdminStatus("❌ Erreur de connexion.");
    }
  };

  const handleRequestLeave = async (e) => {
    e.preventDefault();
    const finalLeaveEmpId = isEmployeeOnly ? currentEmployee?.id : leaveEmpId;
    if (!finalLeaveEmpId) {
      setAdminStatus("⚠️ Profil collaborateur non identifié.");
      return;
    }
    setAdminStatus("⏳ Soumission du congé...");
    try {
      const res = await fetch("/api/proxy/leaves", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          employeeId: finalLeaveEmpId,
          startDate: leaveStartDate,
          endDate: leaveEndDate,
          reason: leaveReason,
          leaveType: leaveType
        })
      });
      if (res.ok) {
        setAdminStatus("✅ Demande de congé créée !");
        fetchAdminData();
      } else {
        const err = await res.json();
        setAdminStatus(`❌ Erreur: ${cleanErrorMessage(err.message)}`);
      }
    } catch (err) {
      setAdminStatus("❌ Erreur de connexion.");
    }
  };

  const handleApproveLeave = async (leaveId) => {
    setAdminStatus("⏳ Approbation du congé...");
    try {
      const res = await fetch(`/api/proxy/leaves/${leaveId}/approve`, {
        method: "PATCH",
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (res.ok) {
        setAdminStatus("✅ Congé approuvé avec succès !");
        // Mise à jour instantanée de l'état local pour un affichage sur place
        setAdminLeaves(prev => prev.map(l => l.id === leaveId ? { ...l, status: "APPROVED", approvedBy: "Manager" } : l));
        fetchAdminData();
      } else {
        const err = await res.json();
        setAdminStatus(`❌ Erreur: ${cleanErrorMessage(err.message)}`);
      }
    } catch (err) {
      setAdminStatus("❌ Erreur de connexion.");
    }
  };

  const handleRejectLeave = async (leaveId) => {
    const reason = prompt("Veuillez indiquer le motif du rejet :");
    if (reason === null) return;
    setAdminStatus("⏳ Rejet du congé...");
    try {
      const res = await fetch(`/api/proxy/leaves/${leaveId}/reject?reason=${encodeURIComponent(reason)}`, {
        method: "PATCH",
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (res.ok) {
        setAdminStatus("✅ Congé rejeté avec succès !");
        // Mise à jour instantanée de l'état local pour un affichage sur place
        setAdminLeaves(prev => prev.map(l => l.id === leaveId ? { ...l, status: "REJECTED", rejectionReason: reason } : l));
        fetchAdminData();
      } else {
        const err = await res.json();
        setAdminStatus(`❌ Erreur: ${cleanErrorMessage(err.message)}`);
      }
    } catch (err) {
      setAdminStatus("❌ Erreur de connexion.");
    }
  };

  // Récupérer la liste des employés
  const fetchEmployees = async () => {
    try {
      const res = await fetch("/api/proxy/employees", {
        method: "GET",
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setEmployees(data.content || []);
        if (data.content && data.content.length > 0) {
          setUploadEmployeeId(data.content[0].id);
        }
      } else if (res.status === 401 || res.status === 403) {
        handleLogout();
      }
    } catch (err) {
      console.error("Erreur de chargement des employés :", err);
    }
  };

  // Action d'upload de document RAG
  const handleUploadDocument = async (e) => {
    e.preventDefault();
    if (!uploadFile) {
      setUploadStatus("⚠️ Veuillez d'abord sélectionner un document (PDF ou image).");
      return;
    }
    setLoadingUpload(true);
    setUploadStatus("");

    const formData = new FormData();
    formData.append("employeeId", uploadEmployeeId);
    formData.append("file", uploadFile);
    formData.append("type", uploadFileType);
    formData.append("description", uploadDescription);

    try {
      const res = await fetch("/api/proxy/documents/upload", {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}` },
        body: formData,
      });

      if (res.ok) {
        setUploadStatus("✅ Document ajouté avec succès aux dossiers de l'employé !");
        setUploadFile(null);
        setUploadDescription("");
        // Réinitialiser l'input file HTML
        const fileInput = document.getElementById("doc-file-input");
        if (fileInput) fileInput.value = "";
      } else {
        const errData = await res.json().catch(() => null);
        setUploadStatus(`⚠️ Erreur : ${errData?.message || "Échec de l'ajout du document."}`);
      }
    } catch (err) {
      setUploadStatus("⚠️ Erreur de connexion avec la passerelle d'upload.");
    } finally {
      setLoadingUpload(false);
    }
  };

  // Action de connexion
  const handleLogin = async (e) => {
    e.preventDefault();
    setLoginError("");
    setLoadingLogin(true);

    try {
      const res = await fetch("/api/proxy/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      const data = await res.json();

      if (res.ok && data.token) {
        localStorage.setItem("hr_jwt_token", data.token);
        localStorage.setItem("hr_user_roles", JSON.stringify(data.roles || []));
        setToken(data.token);
        setUserRoles(data.roles || []);
        
        // Navigation automatique selon les rôles
        const roles = data.roles || [];
        const isEmployeeOnly = roles.includes("ROLE_EMPLOYEE") && 
                               !roles.includes("ROLE_ADMIN") && 
                               !roles.includes("ROLE_HR_MANAGER") && 
                               !roles.includes("ROLE_MANAGER");
        if (isEmployeeOnly) {
          setActiveTab("chatbot");
        } else {
          setActiveTab("dashboard");
        }
      } else {
        setLoginError(data.message || "Identifiants invalides.");
      }
    } catch (err) {
      setLoginError("Impossible de se connecter au serveur.");
    } finally {
      setLoadingLogin(false);
    }
  };

  // Déconnexion
  const handleLogout = () => {
    localStorage.removeItem("hr_jwt_token");
    localStorage.removeItem("hr_user_roles");
    setToken("");
    setUserRoles([]);
    setKpis(null);
    setChatHistory([]);
    setCurrentEmployee(null);
  };

  // Récupérer les KPIs
  const fetchKpis = async () => {
    setLoadingKpis(true);
    try {
      const res = await fetch("/api/proxy/dashboard", {
        method: "GET",
        headers: { "Authorization": `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setKpis(data);
      } else if (res.status === 401 || res.status === 403) {
        handleLogout();
      }
    } catch (err) {
      console.error("Fetch KPIs error:", err);
    } finally {
      setLoadingKpis(false);
    }
  };

  // Calculer la prédiction d'attrition
  const handlePredict = async (e) => {
    if (e) e.preventDefault();
    setLoadingPredict(true);
    setPrediction(null);

    const payload = {
      age: intVal(age),
      monthly_income: floatVal(monthlyIncome),
      years_at_company: intVal(yearsAtCompany),
      job_satisfaction: intVal(jobSatisfaction),
      work_life_balance: intVal(workLifeBalance),
      overtime: intVal(overtime),
      num_promotions: intVal(numPromotions)
    };

    try {
      const res = await fetch("/api/proxy/predict", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const data = await res.json();
        setPrediction(data);
      }
    } catch (err) {
      console.error("Prediction error:", err);
    } finally {
      setLoadingPredict(false);
    }
  };

  const handleLoadEmployeeFeatures = async (employeeId) => {
    if (!employeeId) return;
    setLoadingPredict(true);
    setPrediction(null);
    try {
      const res = await fetch(`/api/proxy/predict?employeeId=${employeeId}`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setAge(data.age || 30);
        setMonthlyIncome(data.monthlyIncome || 10000);
        setYearsAtCompany(data.yearsAtCompany || 3);
        setJobSatisfaction(data.jobSatisfaction || 3);
        setWorkLifeBalance(data.workLifeBalance || 3);
        setOvertime(data.overtime || 0);
        setNumPromotions(data.numPromotions || 0);
        setPrediction(data);
      } else {
        console.error("Failed to load employee features");
      }
    } catch (e) {
      console.error("Error loading features:", e);
    } finally {
      setLoadingPredict(false);
    }
  };

  // Envoyer un message de chat sémantique (RAG)
  const handleSendChat = async (e, customMessage = null) => {
    if (e) e.preventDefault();
    const userMessage = customMessage || query;
    if (!userMessage.trim()) return;

    if (!customMessage) setQuery("");
    setChatHistory(prev => [...prev, { role: "user", content: userMessage }]);
    setLoadingChat(true);

    try {
      const res = await fetch("/api/proxy/chat", {
        method: "POST",
        headers: { 
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ message: userMessage }),
      });

      const responseText = await res.text();
      setChatHistory(prev => [...prev, { role: "bot", content: responseText }]);
    } catch (err) {
      setChatHistory(prev => [...prev, { role: "bot", content: "Erreur de connexion au service de messagerie." }]);
    } finally {
      setLoadingChat(false);
    }
  };

  // Lancement automatique du Plan de Rétention IA (Couplage ML-LLM)
  const handleGenerateRetentionPlan = (probability) => {
    // 1. Basculer vers l'onglet Chatbot
    setActiveTab("chatbot");
    
    // 2. Construire le prompt d'analyse sémantique croisée
    const probPct = (probability * 100).toFixed(0);
    const autoQuery = `Bonjour, notre simulateur de fidélité vient de détecter un risque de départ élevé de ${probPct}% pour un collaborateur (rémunération: ${monthlyIncome} MAD, ancienneté: ${yearsAtCompany} ans, heures supplémentaires: ${overtime === 1 ? 'Oui' : 'Non'}, satisfaction: ${jobSatisfaction}/4). 

Peux-tu analyser la situation au vu de son contrat de travail et nous suggérer un plan d'accompagnement personnalisé et des solutions adaptées (ex: ajustement de salaire, aménagement de congés) ?`;
    
    // 3. Déclencher automatiquement le chat après un court instant
    setTimeout(() => {
      handleSendChat(null, autoQuery);
    }, 200);
  };

  // Action d'audit anti-fraude de note de frais par le modèle ML (Axe C)
  const handleAuditExpense = async (e) => {
    e.preventDefault();
    setLoadingExpense(true);
    setExpenseAnalysis(null);

    const payload = {
      amount: parseFloat(expenseAmount),
      category: expenseCategory,
      day_of_week: expenseDay
    };

    try {
      const res = await fetch("/api/proxy/predict/anomaly", {
        method: "POST",
        headers: { 
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const data = await res.json();
        setExpenseAnalysis(data);
      }
    } catch (err) {
      console.error("Erreur de vérification de dépense :", err);
    } finally {
      setLoadingExpense(false);
    }
  };

  // Demander une Décision Assistée par l'IA RAG (Couplage ML-LLM)
  const handleAskAiDecision = () => {
    if (!expenseAnalysis) return;
    
    // 1. Basculer vers l'onglet Chatbot
    setActiveTab("chatbot");
    
    // 2. Construire le prompt d'évaluation croisée
    const reasonsList = expenseAnalysis.reasons.join(", ");
    const autoQuery = `Bonjour, notre système de contrôle vient d'analyser une note de frais de ${expenseAmount} MAD dans la catégorie ${expenseCategory} le ${expenseDay.toLowerCase()}. 
L'analyse indique : Risque ${expenseAnalysis.risk_level} (Indice d'écart de ${(expenseAnalysis.anomaly_score * 100).toFixed(0)}%) avec pour motifs : ${reasonsList}.

En te basant sur le règlement interne et la politique d'audit de SmartHR, peux-tu me rédiger une recommandation officielle d'acceptation, de mise en attente ou de rejet de cette note de frais ?`;
    
    // 3. Déclencher automatiquement le chat après un court instant
    setTimeout(() => {
      handleSendChat(null, autoQuery);
    }, 200);
  };

  // Helpers de conversion
  const intVal = (val) => parseInt(val, 10);
  const floatVal = (val) => parseFloat(val);

  // === SVG ICON COMPONENTS ===
  const Icon = ({ children, size = 20, ...props }) => (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...props}>{children}</svg>
  );
  const IconDashboard = (p) => <Icon {...p}><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></Icon>;
  const IconBrain = (p) => <Icon {...p}><path d="M12 2a7 7 0 0 0-7 7c0 3 2 5.5 4 7l3 3 3-3c2-1.5 4-4 4-7a7 7 0 0 0-7-7z"/><path d="M12 2v10"/><path d="M8.5 7h7"/></Icon>;
  const IconChat = (p) => <Icon {...p}><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></Icon>;
  const IconSettings = (p) => <Icon {...p}><circle cx="12" cy="12" r="3"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></Icon>;
  const IconUser = (p) => <Icon {...p}><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></Icon>;
  const IconUserPlus = (p) => <Icon {...p}><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></Icon>;
  const IconBuilding = (p) => <Icon {...p}><rect x="4" y="2" width="16" height="20" rx="2"/><path d="M9 22v-4h6v4"/><path d="M8 6h.01M16 6h.01M12 6h.01M8 10h.01M16 10h.01M12 10h.01M8 14h.01M16 14h.01M12 14h.01"/></Icon>;
  const IconClock = (p) => <Icon {...p}><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></Icon>;
  const IconDollar = (p) => <Icon {...p}><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></Icon>;
  const IconCalendar = (p) => <Icon {...p}><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></Icon>;
  const IconUpload = (p) => <Icon {...p}><polyline points="16 16 12 12 8 16"/><line x1="12" y1="12" x2="12" y2="21"/><path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/></Icon>;
  const IconShield = (p) => <Icon {...p}><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></Icon>;
  const IconDownload = (p) => <Icon {...p}><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></Icon>;
  const IconLogOut = (p) => <Icon {...p}><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></Icon>;
  const IconCheck = (p) => <Icon {...p}><polyline points="20 6 9 17 4 12"/></Icon>;
  const IconX = (p) => <Icon {...p}><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></Icon>;
  const IconSend = (p) => <Icon {...p}><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></Icon>;
  const IconZap = (p) => <Icon {...p}><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></Icon>;
  const IconTarget = (p) => <Icon {...p}><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></Icon>;
  const IconFileText = (p) => <Icon {...p}><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></Icon>;
  const IconSparkles = (p) => <Icon {...p}><path d="M12 3l1.5 4.5L18 9l-4.5 1.5L12 15l-1.5-4.5L6 9l4.5-1.5L12 3z"/><path d="M5 19l.5 1.5L7 21l-1.5.5L5 23l-.5-1.5L3 21l1.5-.5L5 19z"/><path d="M19 13l.5 1.5L21 15l-1.5.5L19 17l-.5-1.5L17 15l1.5-.5L19 13z"/></Icon>;
  const IconLock = (p) => <Icon {...p}><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></Icon>;
  const IconMail = (p) => <Icon {...p}><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></Icon>;
  const IconSearch = (p) => <Icon {...p}><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></Icon>;
  const IconActivity = (p) => <Icon {...p}><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></Icon>;
  const IconAlertTriangle = (p) => <Icon {...p}><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></Icon>;

  // Rendu de l'écran de connexion si non connecté
  if (!token) {
    return (
      <div className="login-container">
        <div className="login-card animate-scale-in">
          <div style={{ display: "flex", justifyContent: "center", marginBottom: "24px" }}>
            <div style={{ width: "56px", height: "56px", borderRadius: "16px", background: "var(--lime)", display: "flex", alignItems: "center", justifyContent: "center" }}>
              <IconLock size={28} style={{ color: "var(--lime-ink)" }} />
            </div>
          </div>
          <h2>SmartHR</h2>
          <p className="login-subtitle">Connectez-vous pour piloter l'écosystème RH & IA</p>
          
          <form onSubmit={handleLogin} style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
            <div className="field">
              <label className="field-label">Nom d'utilisateur</label>
              <div style={{ position: "relative" }}>
                <IconUser size={16} style={{ position: "absolute", left: "14px", top: "50%", transform: "translateY(-50%)", color: "var(--ink-tertiary)" }} />
                <input 
                  type="text" 
                  value={username} 
                  onChange={(e) => setUsername(e.target.value)} 
                  required 
                  style={{ paddingLeft: "40px" }}
                  placeholder="ex: hr, admin, employee"
                />
              </div>
            </div>
            
            <div className="field">
              <label className="field-label">Mot de passe</label>
              <div style={{ position: "relative" }}>
                <IconLock size={16} style={{ position: "absolute", left: "14px", top: "50%", transform: "translateY(-50%)", color: "var(--ink-tertiary)" }} />
                <input 
                  type="password" 
                  value={password} 
                  onChange={(e) => setPassword(e.target.value)} 
                  required 
                  style={{ paddingLeft: "40px" }}
                  placeholder="••••••••"
                />
              </div>
            </div>

            {loginError && (
              <div style={{ display: "flex", alignItems: "center", gap: "8px", padding: "12px 16px", borderRadius: "var(--radius-md)", background: "var(--negative-subtle)", color: "var(--negative)", fontSize: "0.85rem", fontWeight: "500" }}>
                <IconAlertTriangle size={16} />
                {loginError}
              </div>
            )}

            <button className="btn-primary" type="submit" disabled={loadingLogin} style={{ marginTop: "4px", padding: "14px 24px" }}>
              {loadingLogin ? "Connexion en cours..." : "Se connecter"}
            </button>
          </form>
        </div>
      </div>
    );
  }

  // Filtrage et pagination des congés pour l'administration ou la liste utilisateur
  const filteredLeaves = adminLeaves.filter(l => {
    const belongsToUser = isHrOrAdmin || l.employeeId === currentEmployee?.id;
    if (!belongsToUser) return false;
    
    if (leaveSearchQuery.trim() === "") return true;
    const fullName = (l.employeeFullName || "").toLowerCase();
    return fullName.includes(leaveSearchQuery.toLowerCase());
  });

  const leavesPerPage = 5;
  const totalLeavePages = Math.max(1, Math.ceil(filteredLeaves.length / leavesPerPage));
  const currentPageSafe = Math.min(leaveCurrentPage, totalLeavePages);
  const paginatedLeaves = filteredLeaves.slice(
    (currentPageSafe - 1) * leavesPerPage,
    currentPageSafe * leavesPerPage
  );

  return (
    <div style={{ display: "flex", minHeight: "100vh", background: "var(--canvas)", color: "var(--ink)", fontFamily: "'Inter', system-ui, sans-serif" }}>
      
      {/* ═══ SIDEBAR ═══ */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <h2>SmartHR</h2>
          <p>Gestion Interne & Assistant</p>
        </div>

        <nav className="sidebar-nav">
          {!isEmployeeOnly && (
            <button 
              className={`sidebar-link ${activeTab === "dashboard" ? "active" : ""}`}
              onClick={() => setActiveTab("dashboard")}
            >
              <span className="icon"><IconDashboard size={18} /></span> Tableau de Bord
            </button>
          )}

          {!isEmployeeOnly && (
            <button 
              className={`sidebar-link ${activeTab === "attrition" ? "active" : ""}`}
              onClick={() => setActiveTab("attrition")}
            >
              <span className="icon"><IconActivity size={18} /></span> Fidélisation
            </button>
          )}

          <button 
            className={`sidebar-link ${activeTab === "leaves" ? "active" : ""}`}
            onClick={() => setActiveTab("leaves")}
          >
            <span className="icon"><IconCalendar size={18} /></span> Demandes de Congé
          </button>

          <button 
            className={`sidebar-link ${activeTab === "chatbot" ? "active" : ""}`}
            onClick={() => setActiveTab("chatbot")}
          >
            <span className="icon"><IconChat size={18} /></span> Assistant SmartHR
          </button>

          {isHrOrAdmin && (
            <button 
              className={`sidebar-link ${activeTab === "admin" ? "active" : ""}`}
              onClick={() => setActiveTab("admin")}
            >
              <span className="icon"><IconSettings size={18} /></span> Administration
            </button>
          )}
        </nav>

        <div className="sidebar-profile">
          <div className="user-info">
            <span className="user-label">Connecté</span>
            <span className="user-name">{username}</span>
            <span className="user-role">{formatRoleDisplay()}</span>
          </div>
          <button className="logout-btn" onClick={handleLogout}>
            <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
              <IconLogOut size={14} /> Déconnexion
            </span>
          </button>
        </div>
      </aside>

      {/* ═══ MAIN CONTENT ═══ */}
      <main style={{ marginLeft: "260px", flex: 1, padding: "20px 24px", minWidth: "0", display: "flex", flexDirection: "column", gap: "18px" }}>
        
        {/* Top bar */}
        <div className="topbar">
          <div>
            <h1>
              {activeTab === "dashboard" ? "Tableau de Bord RH" :
               activeTab === "attrition" ? "Fidélisation des Collaborateurs" :
               activeTab === "leaves" ? "Demandes de Congé" :
               activeTab === "chatbot" ? "Assistant SmartHR" : "Console d'Administration"}
            </h1>
            <p className="subtitle">
              {activeTab === "dashboard" ? "Indicateurs de performance de l'entreprise en temps réel" :
               activeTab === "attrition" ? "Analyse de fidélité et risques de départ" :
               activeTab === "leaves" ? "Soumettez ou suivez les demandes d'absences et de congés" :
               activeTab === "chatbot" ? "Posez vos questions sur la vie d'entreprise ou les contrats" : "Gestion opérationnelle et exports intégrés"}
            </p>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: "12px", marginLeft: "auto" }}>
            {quickCheckInStatus && (
              <span className="badge badge-success" style={{ padding: "8px 14px", fontSize: "0.82rem" }}>
                {quickCheckInStatus}
              </span>
            )}
            <button onClick={handleQuickCheckIn} className="checkin-btn">
              <IconClock size={16} /> Pointer ma Présence
            </button>
          </div>
        </div>

      {/* ═══ 1. TABLEAU DE BORD ═══ */}
      {activeTab === "dashboard" && !isEmployeeOnly && (
        <div className="animate-fade-in" style={{ display: "flex", flexDirection: "column", gap: "28px" }}>
          <h2 style={{ fontSize: "1.2rem", fontWeight: "700", color: "var(--ink-secondary)", letterSpacing: "-0.01em" }}>Indicateurs Clés de Performance</h2>
          
          {loadingKpis ? (
            <p style={{ color: "var(--ink-tertiary)" }}>Chargement des données...</p>
          ) : kpis ? (
            <div style={{ display: "flex", flexDirection: "column", gap: "28px" }}>
              <div className="grid-auto stagger">
                <div className="stat-card">
                  <div className="stat-label">Masse Salariale Mensuelle</div>
                  <div className="stat-value">
                    {kpis.monthlyPayrollMass?.toLocaleString("fr-FR", { minimumFractionDigits: 2 })}
                  </div>
                  <div style={{ fontSize: "0.8rem", color: "var(--ink-tertiary)", marginTop: "6px" }}>MAD</div>
                </div>
                
                <div className="stat-card">
                  <div className="stat-label">Salaire Moyen Net</div>
                  <div className="stat-value">
                    {kpis.averageSalary?.toLocaleString("fr-FR", { minimumFractionDigits: 2 })}
                  </div>
                  <div style={{ fontSize: "0.8rem", color: "var(--ink-tertiary)", marginTop: "6px" }}>MAD</div>
                </div>

                <div className="stat-card">
                  <div className="stat-label">Effectif Total</div>
                  <div className="stat-value positive">
                    {kpis.totalEmployees}
                  </div>
                  <div style={{ fontSize: "0.8rem", color: "var(--ink-tertiary)", marginTop: "6px" }}>collaborateurs</div>
                </div>

                <div className="stat-card">
                  <div className="stat-label">Présents du Jour</div>
                  <div className="stat-value positive">
                    {kpis.activeEmployees}
                  </div>
                  <div style={{ fontSize: "0.8rem", color: "var(--ink-tertiary)", marginTop: "6px" }}>actifs aujourd'hui</div>
                </div>
              </div>

              {/* Admin Grid — RAG Upload + ML Audit */}
              <div style={{ display: isHrOrAdmin ? "grid" : "none", gridTemplateColumns: "repeat(auto-fit, minmax(420px, 1fr))", gap: "24px", marginTop: "8px" }}>
                
                {/* RAG Upload */}
                <div className="card">
                  <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "16px" }}>
                    <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--info-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--info)" }}>
                      <IconUpload size={18} />
                    </div>
                    <div>
                      <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Ajout de Documents</h3>
                      <p style={{ fontSize: "0.78rem", color: "var(--ink-tertiary)", margin: 0 }}>Ajoutez les justificatifs et contrats des employés</p>
                    </div>
                  </div>

                  <form onSubmit={handleUploadDocument} style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                    <div className="grid-2">
                      <div className="field">
                        <label className="field-label">Rattacher à</label>
                        <select value={uploadEmployeeId} onChange={(e) => setUploadEmployeeId(e.target.value)}>
                          {employees.map(emp => (<option key={emp.id} value={emp.id}>{emp.firstName} {emp.lastName}</option>))}
                        </select>
                      </div>
                      <div className="field">
                        <label className="field-label">Type de document</label>
                        <select value={uploadFileType} onChange={(e) => setUploadFileType(e.target.value)}>
                          <option value="CONTRACT">Contrat</option>
                          <option value="PAYSLIP">Bulletin de paie</option>
                          <option value="DIPLOMA">Diplôme</option>
                          <option value="CERTIFICATE">Certificat</option>
                          <option value="CNSS">CNSS</option>
                          <option value="OTHER">Autre</option>
                        </select>
                      </div>
                    </div>
                    <div className="field">
                      <label className="field-label">Fichier (PDF, Image)</label>
                      <input type="file" id="doc-file-input" accept=".pdf,.png,.jpg,.jpeg" onChange={(e) => setUploadFile(e.target.files[0])} />
                    </div>
                    <div className="field">
                      <label className="field-label">Description</label>
                      <textarea value={uploadDescription} onChange={(e) => setUploadDescription(e.target.value)} placeholder="Description du document..." rows="2" style={{ minHeight: "60px" }} />
                    </div>
                    {uploadStatus && (
                      <div className={`status-banner`} style={{ background: uploadStatus.includes("✅") ? "var(--positive-subtle)" : "var(--negative-subtle)", borderLeftColor: uploadStatus.includes("✅") ? "var(--positive)" : "var(--negative)" }}>
                        {uploadStatus}
                      </div>
                    )}
                    <button type="submit" disabled={loadingUpload} className="btn-primary" style={{ alignSelf: "flex-start", opacity: loadingUpload ? 0.7 : 1 }}>
                      <span style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                        <IconZap size={16} /> {loadingUpload ? "Lecture du document..." : "Ajouter le document"}
                      </span>
                    </button>
                  </form>
                </div>

                {/* ML Audit */}
                <div className="card">
                  <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "16px" }}>
                    <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--negative-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--negative)" }}>
                      <IconShield size={18} />
                    </div>
                    <div>
                      <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Vérification des Notes de Frais</h3>
                      <p style={{ fontSize: "0.78rem", color: "var(--ink-tertiary)", margin: 0 }}>Vérification de la conformité des dépenses</p>
                    </div>
                  </div>

                  <form onSubmit={handleAuditExpense} style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                    <div className="grid-2">
                      <div className="field">
                        <label className="field-label">Montant (MAD)</label>
                        <input type="number" value={expenseAmount} onChange={(e) => setExpenseAmount(e.target.value)} />
                      </div>
                      <div className="field">
                        <label className="field-label">Catégorie</label>
                        <select value={expenseCategory} onChange={(e) => setExpenseCategory(e.target.value)}>
                          <option value="MEAL">Repas</option>
                          <option value="TRAVEL">Transport</option>
                          <option value="LODGING">Hébergement</option>
                          <option value="OTHER">Autre</option>
                        </select>
                      </div>
                    </div>
                    <div className="field">
                      <label className="field-label">Jour de la dépense</label>
                      <select value={expenseDay} onChange={(e) => setExpenseDay(e.target.value)}>
                        <option value="MONDAY">Lundi</option>
                        <option value="TUESDAY">Mardi</option>
                        <option value="WEDNESDAY">Mercredi</option>
                        <option value="THURSDAY">Jeudi</option>
                        <option value="FRIDAY">Vendredi</option>
                        <option value="SATURDAY">Samedi</option>
                        <option value="SUNDAY">Dimanche</option>
                      </select>
                    </div>

                    {expenseAnalysis && (
                      <div className="animate-fade-in" style={{ padding: "16px", borderRadius: "var(--radius-lg)", background: expenseAnalysis.is_anomaly ? "var(--negative-subtle)" : "var(--positive-subtle)", border: `1px solid ${expenseAnalysis.is_anomaly ? "rgba(208,50,56,0.2)" : "rgba(46,173,75,0.2)"}` }}>
                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "8px" }}>
                          <span style={{ fontSize: "0.85rem", fontWeight: "700" }}>Diagnostic de conformité</span>
                          <span className={`badge ${expenseAnalysis.is_anomaly ? "badge-danger" : "badge-success"}`}>
                            {expenseAnalysis.risk_level}
                          </span>
                        </div>
                        <div style={{ fontSize: "0.8rem", color: "var(--ink-secondary)", marginBottom: "8px" }}>
                          Indice d'écart : {(expenseAnalysis.anomaly_score * 100).toFixed(0)}%
                        </div>
                        <ul style={{ paddingLeft: "16px", fontSize: "0.8rem", display: "flex", flexDirection: "column", gap: "4px", margin: "0 0 12px 0", color: "var(--ink-secondary)" }}>
                          {expenseAnalysis.reasons.map((reason, idx) => (<li key={idx}>{reason}</li>))}
                        </ul>
                        {expenseAnalysis.is_anomaly && (
                          <button type="button" onClick={handleAskAiDecision} className="btn-outline btn-sm" style={{ width: "100%" }}>
                            <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
                              <IconSparkles size={14} /> Demander l'avis de l'assistant
                            </span>
                          </button>
                        )}
                      </div>
                    )}

                    <button type="submit" disabled={loadingExpense} className="btn-primary" style={{ alignSelf: "flex-start", opacity: loadingExpense ? 0.7 : 1 }}>
                      <span style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                        <IconTarget size={16} /> {loadingExpense ? "Vérification en cours..." : "Vérifier la dépense"}
                      </span>
                    </button>
                  </form>
                </div>
              </div>
            </div>
          ) : (
            <p style={{ color: "var(--negative)" }}>Impossible de charger le tableau de bord.</p>
          )}
        </div>
      )}

      {/* ═══ 2. SIMULATION ATTRITION ML ═══ */}
      {activeTab === "attrition" && !isEmployeeOnly && (
        <div className="animate-fade-in grid-admin">
          
          {/* Formulaire */}
          <div className="card">
            <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
              <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--warning-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--warning)" }}>
                <IconActivity size={18} />
              </div>
              <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Caractéristiques du Collaborateur</h3>
            </div>
            
            <form onSubmit={handlePredict} style={{ display: "flex", flexDirection: "column", gap: "18px" }}>
              <div className="field">
                <label className="field-label">Pré-remplir depuis les données réelles d'un collaborateur</label>
                <select onChange={(e) => handleLoadEmployeeFeatures(e.target.value)} defaultValue="">
                  <option value="">— Choisir un collaborateur —</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.firstName} {emp.lastName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="grid-2">
                <div className="field">
                  <label className="field-label">Âge ({age} ans)</label>
                  <input type="range" min="18" max="65" value={age} onChange={(e) => setAge(e.target.value)} />
                </div>
                <div className="field">
                  <label className="field-label">Ancienneté ({yearsAtCompany} ans)</label>
                  <input type="range" min="1" max="20" value={yearsAtCompany} onChange={(e) => setYearsAtCompany(e.target.value)} />
                </div>
              </div>

              <div className="field">
                <label className="field-label">Salaire Brut Mensuel (MAD)</label>
                <input type="number" value={monthlyIncome} onChange={(e) => setMonthlyIncome(e.target.value)} min="3000" max="100000" />
              </div>

              <div className="grid-2">
                <div className="field">
                  <label className="field-label">Satisfaction au Travail</label>
                  <select value={jobSatisfaction} onChange={(e) => setJobSatisfaction(e.target.value)}>
                    <option value="1">1 — Très Basse</option>
                    <option value="2">2 — Basse</option>
                    <option value="3">3 — Haute</option>
                    <option value="4">4 — Très Haute</option>
                  </select>
                </div>
                <div className="field">
                  <label className="field-label">Équilibre Vie Pro/Perso</label>
                  <select value={workLifeBalance} onChange={(e) => setWorkLifeBalance(e.target.value)}>
                    <option value="1">1 — Mauvais</option>
                    <option value="2">2 — Moyen</option>
                    <option value="3">3 — Bon</option>
                    <option value="4">4 — Excellent</option>
                  </select>
                </div>
              </div>

              <div className="grid-2">
                <div className="field">
                  <label className="field-label">Heures Supplémentaires</label>
                  <select value={overtime} onChange={(e) => setOvertime(e.target.value)}>
                    <option value="0">Non</option>
                    <option value="1">Oui</option>
                  </select>
                </div>
                <div className="field">
                  <label className="field-label">Promotions (5 ans)</label>
                  <input type="number" min="0" max="5" value={numPromotions} onChange={(e) => setNumPromotions(e.target.value)} />
                </div>
              </div>

              <button className="btn-primary" type="submit" disabled={loadingPredict} style={{ marginTop: "4px" }}>
                <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "8px" }}>
                  <IconTarget size={16} /> {loadingPredict ? "Analyse en cours..." : "Analyser la fidélité"}
                </span>
              </button>
            </form>
          </div>

          {/* Résultat — Jauge */}
          <div className="card" style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
            {prediction ? (
              <div className="animate-fade-in" style={{ width: "100%", textAlign: "center" }}>
                <h3 style={{ fontSize: "1.1rem", fontWeight: "700", marginBottom: "24px" }}>Analyse de Fidélité</h3>
                
                <div className="gauge-container">
                  <svg width="220" height="130" viewBox="0 0 220 130">
                    <path d="M 20 110 A 90 90 0 0 1 200 110" fill="none" stroke="var(--canvas-elevated)" strokeWidth="16" strokeLinecap="round" />
                    <path 
                      d="M 20 110 A 90 90 0 0 1 200 110" 
                      fill="none" 
                      stroke={prediction.probability >= 0.7 ? "var(--negative)" : prediction.probability >= 0.4 ? "var(--warning)" : "var(--positive)"} 
                      strokeWidth="16" 
                      strokeLinecap="round" 
                      strokeDasharray="502"
                      strokeDashoffset={282.7 - (prediction.probability * 282.7)}
                      style={{ transition: "stroke-dashoffset 0.8s var(--ease-out)" }}
                    />
                  </svg>
                  <div className="gauge-value">
                    <div className="number">{Math.round(prediction.probability * 100)}%</div>
                    <div className="label">Risque : {prediction.risk_level}</div>
                  </div>
                </div>

                <div style={{ textAlign: "left", marginTop: "20px" }}>
                  <h4 style={{ fontSize: "0.9rem", fontWeight: "700", marginBottom: "10px", display: "flex", alignItems: "center", gap: "6px" }}>
                    <IconAlertTriangle size={16} style={{ color: "var(--warning)" }} /> Facteurs déterminants
                  </h4>
                  <ul style={{ listStyleType: "none", paddingLeft: "0", display: "flex", flexDirection: "column", gap: "6px" }}>
                    {prediction.factors?.map((factor, index) => (
                      <li key={index} style={{ fontSize: "0.85rem", display: "flex", alignItems: "center", gap: "8px", color: "var(--ink-secondary)" }}>
                        <span style={{ width: "6px", height: "6px", borderRadius: "50%", background: prediction.probability >= 0.5 ? "var(--negative)" : "var(--positive)", flexShrink: 0 }} />
                        {factor}
                      </li>
                    ))}
                  </ul>
                </div>

                <button onClick={() => handleGenerateRetentionPlan(prediction.probability)} className="btn-primary" style={{ marginTop: "24px", width: "100%" }}>
                  <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "8px" }}>
                    <IconSparkles size={16} /> Créer un plan d'accompagnement
                  </span>
                </button>
              </div>
            ) : (
              <div className="empty-state">
                <div className="icon"><IconTarget size={48} /></div>
                <p>Soumettez le profil du collaborateur pour analyser son taux de risque d'attrition.</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ═══ 3. DEMANDES DE CONGÉ ═══ */}
      {activeTab === "leaves" && (
        <div className="animate-fade-in grid-admin">
          {/* Formulaire de création de congé */}
          <div className="card">
            <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
              <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--positive-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--positive)" }}>
                <IconCalendar size={18} />
              </div>
              <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Faire une Demande de Congé</h3>
            </div>
            
            <form onSubmit={handleRequestLeave} style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
              {isHrOrAdmin ? (
                <div className="field">
                  <label className="field-label">Collaborateur</label>
                  <select value={leaveEmpId} onChange={(e) => setLeaveEmpId(e.target.value)} required>
                    <option value="">— Choisir un employé —</option>
                    {adminEmployees.map((emp) => (<option key={emp.id} value={emp.id}>{emp.firstName} {emp.lastName}</option>))}
                  </select>
                </div>
              ) : (
                <div className="field" style={{ padding: "12px", background: "var(--canvas-elevated)", borderRadius: "var(--radius-md)", border: "1px solid var(--border)" }}>
                  <span style={{ fontSize: "0.8rem", color: "var(--ink-secondary)", fontWeight: "600" }}>Demandeur :</span>
                  <span style={{ fontSize: "0.9rem", fontWeight: "700", marginLeft: "6px" }}>{currentEmployee ? `${currentEmployee.firstName} ${currentEmployee.lastName}` : username}</span>
                </div>
              )}
              
              <div className="grid-2">
                <div className="field">
                  <label className="field-label">Date de début</label>
                  <input type="date" value={leaveStartDate} onChange={(e) => setLeaveStartDate(e.target.value)} required />
                </div>
                <div className="field">
                  <label className="field-label">Date de fin</label>
                  <input type="date" value={leaveEndDate} onChange={(e) => setLeaveEndDate(e.target.value)} required />
                </div>
              </div>
              <div className="grid-2">
                <div className="field">
                  <label className="field-label">Motif</label>
                  <input type="text" placeholder="Ex: Congés annuels" value={leaveReason} onChange={(e) => setLeaveReason(e.target.value)} required />
                </div>
                <div className="field">
                  <label className="field-label">Type</label>
                  <select value={leaveType} onChange={(e) => setLeaveType(e.target.value)}>
                    <option value="ANNUAL">Annuel</option>
                    <option value="SICK">Maladie</option>
                    <option value="UNPAID">Sans solde</option>
                    <option value="MATERNITY">Maternité</option>
                  </select>
                </div>
              </div>

              {adminStatus && activeTab === "leaves" && (
                <div className="status-banner" style={{ marginTop: "10px" }}>{adminStatus}</div>
              )}

              <button type="submit" className="btn-primary" style={{ marginTop: "8px" }}>
                <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
                  <IconZap size={16} /> Soumettre la Demande
                </span>
              </button>
            </form>
          </div>

          {/* Liste de suivi & décisions */}
          <div className="card">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px", flexWrap: "wrap", gap: "12px" }}>
              <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>
                {isHrOrAdmin ? `Toutes les Demandes (${filteredLeaves.length})` : "Mes Demandes & Statuts"}
              </h3>
              
              {/* Filtre de recherche par nom */}
              <input
                type="text"
                placeholder="Rechercher par nom..."
                value={leaveSearchQuery}
                onChange={(e) => {
                  setLeaveSearchQuery(e.target.value);
                  setLeaveCurrentPage(1);
                }}
                style={{
                  padding: "6px 12px",
                  fontSize: "0.85rem",
                  borderRadius: "var(--radius-sm)",
                  border: "1px solid var(--border)",
                  background: "var(--surface-subtle)",
                  color: "var(--ink-primary)",
                  maxWidth: "200px"
                }}
              />
            </div>
            
            <div style={{ display: "flex", flexDirection: "column", gap: "10px", minHeight: "200px", maxHeight: "450px", overflowY: "auto" }}>
              {paginatedLeaves.length === 0 ? (
                <p style={{ color: "var(--ink-tertiary)", fontSize: "0.9rem", textAlign: "center", padding: "40px 0" }}>
                  {leaveSearchQuery.trim() !== "" ? "Aucune demande ne correspond à ce nom." : "Aucune demande enregistrée."}
                </p>
              ) : (
                paginatedLeaves.map((l, i) => (
                  <div key={l.id || i} className="list-item" style={{ flexDirection: "column", alignItems: "stretch", gap: "8px" }}>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <span style={{ fontWeight: "700", fontSize: "0.9rem" }}>{l.employeeFullName || "Collaborateur"}</span>
                      <span className={`badge ${l.status === "APPROVED" ? "badge-success" : l.status === "REJECTED" ? "badge-danger" : "badge-warning"}`}>
                        {l.status}
                      </span>
                    </div>
                    <div style={{ fontSize: "0.82rem", color: "var(--ink-secondary)" }}>
                      Du {l.startDate} au {l.endDate} — {l.reason}
                    </div>
                    {isHrOrAdmin && l.status === "PENDING" && (
                      <div style={{ display: "flex", gap: "8px", marginTop: "4px" }}>
                        <button onClick={() => handleApproveLeave(l.id)} className="btn-success btn-sm" style={{ flex: 1 }}>
                          <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "4px" }}>
                            <IconCheck size={14} /> Approuver
                          </span>
                        </button>
                        <button onClick={() => handleRejectLeave(l.id)} className="btn-danger btn-sm" style={{ flex: 1 }}>
                          <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "4px" }}>
                            <IconX size={14} /> Rejeter
                          </span>
                        </button>
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>

            {/* Contrôles de pagination */}
            {totalLeavePages > 1 && (
              <div style={{ display: "flex", justifyContent: "center", alignItems: "center", gap: "12px", marginTop: "16px", paddingTop: "12px", borderTop: "1px solid var(--border)" }}>
                <button
                  disabled={currentPageSafe === 1}
                  onClick={() => setLeaveCurrentPage(prev => Math.max(1, prev - 1))}
                  className="btn-secondary btn-sm"
                  style={{ padding: "4px 8px", fontSize: "0.85rem" }}
                >
                  Précédent
                </button>
                <span style={{ fontSize: "0.85rem", color: "var(--ink-secondary)" }}>
                  Page {currentPageSafe} sur {totalLeavePages}
                </span>
                <button
                  disabled={currentPageSafe === totalLeavePages}
                  onClick={() => setLeaveCurrentPage(prev => Math.min(totalLeavePages, prev + 1))}
                  className="btn-secondary btn-sm"
                  style={{ padding: "4px 8px", fontSize: "0.85rem" }}
                >
                  Suivant
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ═══ 4. ASSISTANT CHATBOT ═══ */}
      {activeTab === "chatbot" && (
        <div className="card animate-fade-in" style={{ display: "flex", flexDirection: "column", height: "650px", padding: 0, overflow: "hidden" }}>
          <div style={{ padding: "20px 24px", borderBottom: "1px solid var(--border)" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <div style={{ width: "32px", height: "32px", borderRadius: "var(--radius-sm)", background: "var(--lime-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--positive)" }}>
                <IconChat size={16} />
              </div>
              <div>
                <h3 style={{ fontSize: "1rem", fontWeight: "700", margin: 0 }}>Assistant SmartHR</h3>
                <p style={{ fontSize: "0.78rem", color: "var(--ink-tertiary)", margin: 0 }}>Posez vos questions sur la vie d'entreprise ou les collaborateurs</p>
              </div>
            </div>
          </div>
          
          <div style={{ flex: 1, overflowY: "auto", padding: "20px 24px", display: "flex", flexDirection: "column", gap: "12px" }}>
            {chatHistory.length === 0 && (
              <div className="empty-state" style={{ marginTop: "80px" }}>
                <div className="icon"><IconSparkles size={48} /></div>
                <p>Posez-moi des questions sur les collaborateurs, règlements de travail ou les dernières lois RH.</p>
              </div>
            )}
            
            {chatHistory.map((msg, index) => (
              <div key={index} className={msg.role === "user" ? "chat-bubble-user" : "chat-bubble-bot"}>
                <div style={{ fontSize: "0.7rem", color: msg.role === "user" ? "rgba(26,58,10,0.6)" : "var(--ink-tertiary)", marginBottom: "4px", fontWeight: "600" }}>
                  {msg.role === "user" ? "Vous" : "Assistant SmartHR"}
                </div>
                <div style={{ whiteSpace: "pre-wrap" }}>{msg.content}</div>
              </div>
            ))}
            
            {loadingChat && (
              <div className="chat-bubble-bot animate-pulse">
                <span style={{ fontSize: "0.85rem", color: "var(--ink-tertiary)" }}>L'assistant prépare votre réponse...</span>
              </div>
            )}
            <div ref={chatEndRef} />
          </div>

          <form onSubmit={handleSendChat} style={{ padding: "16px 24px", borderTop: "1px solid var(--border)", display: "flex", gap: "10px", background: "var(--canvas-elevated)" }}>
            <input 
              type="text" 
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Ex: Quel est le salaire actuel de Mohamed Alami ?" 
              style={{ flex: 1 }}
              disabled={loadingChat}
            />
            <button className="btn-primary btn-icon" type="submit" disabled={loadingChat || !query.trim()} style={{ width: "44px", height: "44px", borderRadius: "var(--radius-md)" }}>
              <IconSend size={18} />
            </button>
          </form>
        </div>
      )}

      {/* ═══ 5. CONSOLE D'ADMINISTRATION ═══ */}
      {activeTab === "admin" && isHrOrAdmin && (
        <div className="animate-fade-in" style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
          
          {/* Tab bar */}
          <div className="tab-bar">
            {[
              { key: "employees", icon: <IconUserPlus size={15} />, label: "Collaborateurs" },
              { key: "departments", icon: <IconBuilding size={15} />, label: "Départements" },
              { key: "attendance", icon: <IconClock size={15} />, label: "Pointages" },
              { key: "payroll", icon: <IconDollar size={15} />, label: "Bulletins de Paie" },
            ].map(tab => (
              <button 
                key={tab.key}
                onClick={() => setAdminSubTab(tab.key)}
                className={`tab-button ${adminSubTab === tab.key ? "active" : ""}`}
              >
                <span style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  {tab.icon} {tab.label}
                </span>
              </button>
            ))}
          </div>

          {adminStatus && activeTab === "admin" && (
            <div className="status-banner">{adminStatus}</div>
          )}

          {/* SUB-TAB: Collaborateurs */}
          {adminSubTab === "employees" && (
            <div className="animate-fade-in grid-admin">
              <div className="card">
                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
                  <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--lime-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--positive)" }}>
                    <IconUserPlus size={18} />
                  </div>
                  <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Nouveau Collaborateur</h3>
                </div>
                <form onSubmit={handleCreateEmployee} style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
                  <div className="grid-2">
                    <input type="text" placeholder="Prénom" value={empFirstName} onChange={(e) => setEmpFirstName(e.target.value)} required />
                    <input type="text" placeholder="Nom" value={empLastName} onChange={(e) => setEmpLastName(e.target.value)} required />
                  </div>
                  <input type="email" placeholder="Adresse email professionnelle" value={empEmail} onChange={(e) => setEmpEmail(e.target.value)} required />
                  <div className="grid-2">
                    <input type="text" placeholder="CIN" value={empCIN} onChange={(e) => setEmpCIN(e.target.value)} required />
                    <select value={empGender} onChange={(e) => setEmpGender(e.target.value)}>
                      <option value="MALE">Homme</option>
                      <option value="FEMALE">Femme</option>
                    </select>
                  </div>
                  <input type="text" placeholder="IBAN / RIB" value={empRIB} onChange={(e) => setEmpRIB(e.target.value)} required />
                  <input type="text" placeholder="Numéro CNSS" value={empCNSS} onChange={(e) => setEmpCNSS(e.target.value)} required />
                  <input type="text" placeholder="Adresse physique" value={empAddress} onChange={(e) => setEmpAddress(e.target.value)} required />
                  <button type="submit" className="btn-primary">
                    <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
                      <IconZap size={16} /> Enregistrer le Collaborateur
                    </span>
                  </button>
                </form>
              </div>

              <div className="card">
                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
                  <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--info-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--info)" }}>
                    <IconDownload size={18} />
                  </div>
                  <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Exports & Rapports</h3>
                </div>
                <p style={{ color: "var(--ink-secondary)", fontSize: "0.85rem", marginBottom: "20px" }}>
                  Exportez le registre légal des employés sous format tabulaire ou document scellé.
                </p>
                <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
                  <button 
                    onClick={async (e) => {
                      e.preventDefault();
                      setAdminStatus("Préparation de l'export Excel...");
                      try {
                        const res = await fetch("/api/proxy/employees/export/excel", { headers: { Authorization: `Bearer ${token}` } });
                        if (res.ok) { const blob = await res.blob(); const url = window.URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = 'employees_export.xlsx'; document.body.appendChild(a); a.click(); a.remove(); setAdminStatus("✅ Fichier Excel téléchargé !"); }
                      } catch (err) { setAdminStatus("❌ Erreur de génération."); }
                    }}
                    className="btn-primary"
                  >
                    <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
                      <IconFileText size={16} /> Exporter le Registre (Excel)
                    </span>
                  </button>
                  
                  <button 
                    onClick={async (e) => {
                      e.preventDefault();
                      setAdminStatus("Préparation de l'export PDF...");
                      try {
                        const res = await fetch("/api/proxy/employees/export/pdf", { headers: { Authorization: `Bearer ${token}` } });
                        if (res.ok) { const blob = await res.blob(); const url = window.URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = 'employees_export.pdf'; document.body.appendChild(a); a.click(); a.remove(); setAdminStatus("✅ Fichier PDF téléchargé !"); }
                      } catch (err) { setAdminStatus("❌ Erreur de génération."); }
                    }}
                    className="btn-danger"
                  >
                    <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
                      <IconDownload size={16} /> Exporter le Registre (PDF)
                    </span>
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* SUB-TAB: Départements */}
          {adminSubTab === "departments" && (
            <div className="animate-fade-in grid-admin">
              <div className="card">
                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
                  <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--info-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--info)" }}>
                    <IconBuilding size={18} />
                  </div>
                  <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Nouveau Département</h3>
                </div>
                <form onSubmit={handleCreateDepartment} style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
                  <input type="text" placeholder="Nom du département (ex: R&D)" value={deptName} onChange={(e) => setDeptName(e.target.value)} required />
                  <textarea placeholder="Description opérationnelle" value={deptDescription} onChange={(e) => setDeptDescription(e.target.value)} required rows="3" />
                  <input type="number" placeholder="Budget Annuel Alloué (MAD)" value={deptBudget} onChange={(e) => setDeptBudget(e.target.value)} required />
                  <button type="submit" className="btn-primary">
                    <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
                      <IconBuilding size={16} /> Créer le Département
                    </span>
                  </button>
                </form>
              </div>

              <div className="card">
                <h3 style={{ fontSize: "1.1rem", fontWeight: "700", marginBottom: "16px" }}>Structure ({adminDepartments.length})</h3>
                <div style={{ display: "flex", flexDirection: "column", gap: "10px", maxHeight: "380px", overflowY: "auto" }}>
                  {adminDepartments.length === 0 ? (
                    <p style={{ color: "var(--ink-tertiary)", fontSize: "0.9rem" }}>Aucun département enregistré.</p>
                  ) : (
                    adminDepartments.map((d, i) => (
                      <div key={i} className="list-item">
                        <div>
                          <div style={{ fontWeight: "700", fontSize: "0.9rem" }}>{d.name}</div>
                          <div style={{ fontSize: "0.78rem", color: "var(--ink-tertiary)", marginTop: "2px" }}>{d.description}</div>
                        </div>
                        <div style={{ fontWeight: "700", color: "var(--positive)", fontSize: "0.9rem", whiteSpace: "nowrap" }}>
                          {d.budget?.toLocaleString("fr-FR")} MAD
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
          )}

          {/* SUB-TAB: Pointages */}
          {adminSubTab === "attendance" && (
            <div className="animate-fade-in" style={{ display: "flex", justifyContent: "center" }}>
              <div className="card" style={{ width: "100%", maxWidth: "560px" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
                  <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--positive-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--positive)" }}>
                    <IconClock size={18} />
                  </div>
                  <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Pointage d'Arrivée (Check-In)</h3>
                </div>
                <form onSubmit={handleCheckIn} style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                  <div className="field">
                    <label className="field-label">Sélectionner le collaborateur</label>
                    <select value={checkInEmpId} onChange={(e) => setCheckInEmpId(e.target.value)} required>
                      <option value="">— Choisir un employé —</option>
                      {adminEmployees.map((emp) => (<option key={emp.id} value={emp.id}>{emp.firstName} {emp.lastName} ({emp.employeeNumber})</option>))}
                    </select>
                  </div>
                  <div className="field">
                    <label className="field-label">Note descriptive</label>
                    <input type="text" placeholder="Ex: Arrivée bureau Casablanca" value={checkInNotes} onChange={(e) => setCheckInNotes(e.target.value)} required />
                  </div>
                  <button type="submit" className="btn-primary">
                    <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
                      <IconClock size={16} /> Enregistrer le Pointage
                    </span>
                  </button>
                </form>
              </div>
            </div>
          )}

          {/* SUB-TAB: Salaires */}
          {adminSubTab === "payroll" && (
            <div className="animate-fade-in" style={{ display: "flex", justifyContent: "center" }}>
              <div className="card" style={{ width: "100%", maxWidth: "620px" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
                  <div style={{ width: "36px", height: "36px", borderRadius: "var(--radius-md)", background: "var(--lime-subtle)", display: "flex", alignItems: "center", justifyContent: "center", color: "var(--positive)" }}>
                    <IconDollar size={18} />
                  </div>
                  <h3 style={{ fontSize: "1.1rem", fontWeight: "700", margin: 0 }}>Bulletin de Paie Mensuel</h3>
                </div>
                <form onSubmit={handleGeneratePayroll} style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                  <div className="field">
                    <label className="field-label">Sélectionner le collaborateur</label>
                    <select value={payrollEmpId} onChange={(e) => setPayrollEmpId(e.target.value)} required>
                      <option value="">— Choisir un employé —</option>
                      {adminEmployees.map((emp) => (<option key={emp.id} value={emp.id}>{emp.firstName} {emp.lastName}</option>))}
                    </select>
                  </div>
                  <div className="grid-2">
                    <div className="field">
                      <label className="field-label">Mois concerné</label>
                      <input type="date" value={payrollMonth} onChange={(e) => setPayrollMonth(e.target.value)} required />
                    </div>
                    <div className="field">
                      <label className="field-label">Prime Exceptionnelle (MAD)</label>
                      <input type="number" value={payrollBonus} onChange={(e) => setPayrollBonus(e.target.value)} />
                    </div>
                  </div>
                  <div className="grid-2">
                    <div className="field">
                      <label className="field-label">Heures Supplémentaires</label>
                      <input type="number" value={payrollOvertime} onChange={(e) => setPayrollOvertime(e.target.value)} />
                    </div>
                    <div className="field">
                      <label className="field-label">Déductions (MAD)</label>
                      <input type="number" value={payrollDeductions} onChange={(e) => setPayrollDeductions(e.target.value)} />
                    </div>
                  </div>
                  <button type="submit" className="btn-primary">
                    <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "6px" }}>
                      <IconDollar size={16} /> Calculer et Enregistrer
                    </span>
                  </button>
                </form>
              </div>
            </div>
          )}
        </div>
      )}
      </main>
    </div>
  );
}
