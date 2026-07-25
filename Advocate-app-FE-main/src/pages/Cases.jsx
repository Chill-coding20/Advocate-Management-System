import React, { useState, useEffect, useCallback, useRef } from "react";
import { useLocation } from "react-router-dom";
import axios from "axios";
import { useLoading } from "../contexts/LoadingContext";
import Select from "react-select";
import { FiFolder, FiEye, FiDownload, FiX, FiUpload, FiFile, FiClock, FiEdit2, FiTrash2 } from "react-icons/fi";
import CaseTimeline from "../components/CaseTimeline.jsx";
import { useToast } from "../contexts/ToastContext.jsx";
import ReportService from "../services/ReportService";
import { formatCurrency } from "../utils/formatCurrency";
import Pagination from "../components/Pagination";
import usePagination from "../hooks/usePagination";
import { InlineLoader } from "../components/Loader";
import "../assets/styles/Cases.css";

const customSelectStyles = {
  control: (base, state) => ({
    ...base,
    backgroundColor: "var(--bg-primary)",
    borderColor: state.isFocused ? "var(--accent-blue)" : "var(--border-color)",
    color: "var(--text-main)",
    borderRadius: "8px",
    padding: "2px",
    boxShadow: "none",
    "&:hover": {
      borderColor: "var(--accent-blue)"
    }
  }),
  menu: (base) => ({
    ...base,
    backgroundColor: "var(--bg-secondary)",
    border: "1px solid var(--border-color)",
    borderRadius: "8px",
    boxShadow: "var(--shadow-md)",
    zIndex: 9999
  }),
  option: (base, state) => ({
    ...base,
    backgroundColor: state.isSelected 
      ? "var(--accent-blue)" 
      : state.isFocused 
        ? "var(--border-color)" 
        : "transparent",
    color: state.isSelected 
      ? "#ffffff" 
      : "var(--text-main)",
    cursor: "pointer",
    "&:active": {
      backgroundColor: "var(--accent-blue)"
    }
  }),
  singleValue: (base) => ({
    ...base,
    color: "var(--text-main)"
  }),
  placeholder: (base) => ({
    ...base,
    color: "var(--text-muted)"
  }),
  input: (base) => ({
    ...base,
    color: "var(--text-main)"
  })
};

function Cases() {
  const [cases, setCases] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [clients, setClients] = useState([]);
  const location = useLocation();
  const [newCase, setNewCase] = useState({
    caseNumber: "",
    caseTitle: "",
    caseType: "",
    courtLevel: "",
    status: "",
    amount: "",
    description: "",
    clientId: "",
  });
  const [showModal, setShowModal] = useState(false);
  const [editCaseId, setEditCaseId] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [caseNumberError, setCaseNumberError] = useState("");
  const [showArchived, setShowArchived] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [highlightedId, setHighlightedId] = useState(null);
  const token = localStorage.getItem("token");
  const { withLoading } = useLoading();
  const { success, error, warning, info } = useToast();
  const { page, setPage, size, setSize } = usePagination({ defaultSize: 20, resetOn: [searchKeyword, showArchived] });
  const [pageLoading, setPageLoading] = useState(true);
  const searchedFromGlobalNav = useRef(!!location.state?.search);

  // Document tab state
  const [showCaseDocs, setShowCaseDocs] = useState(false);
  const [docCase, setDocCase] = useState(null);
  const [caseDocs, setCaseDocs] = useState([]);
  const [caseDocsLoading, setCaseDocsLoading] = useState(false);
  const [uploadDocFile, setUploadDocFile] = useState(null);

  // Timeline state
  const [showTimeline, setShowTimeline] = useState(false);
  const [timelineCase, setTimelineCase] = useState(null);

  const openTimeline = (c) => {
    setTimelineCase(c);
    setShowTimeline(true);
  };

  // ---------------- FETCH CASES ----------------
  const fetchCases = useCallback(async () => {
    setPageLoading(true);
    try {
      const params = { page, size };
      if (searchKeyword.trim()) params.keyword = searchKeyword;
      if (showArchived) params.archived = true;
      const response = await axios.get("/api/cases", {
        headers: { Authorization: `Bearer ${token}` },
        params,
      });
      setCases(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);
      setErrorMessage("");
    } catch (error) {
      console.error("Error fetching cases:", error);
      const errData = error.response?.data;
      setErrorMessage(typeof errData === "string" ? errData : (errData?.message || "Failed to fetch cases."));
    } finally {
      setPageLoading(false);
    }
  }, [token, page, size, searchKeyword, showArchived]);

  // ---------------- FETCH CLIENTS ----------------
  const fetchClients = async () => {
    try {
      const response = await axios.get("/api/clients/my-clients", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setClients(response.data);
    } catch (error) {
      console.error("Error fetching clients:", error);
      setErrorMessage("Failed to fetch clients. Check server/CORS.");
    }
  };

  useEffect(() => {
    if (!token) {
      setErrorMessage("Please login first.");
      return;
    }
    fetchClients();
  }, [token]);

  useEffect(() => {
    if (!token) return;
    if (searchedFromGlobalNav.current) {
      searchedFromGlobalNav.current = false;
      return;
    }
    fetchCases();
  }, [fetchCases, token]);

  // AI Assistant: open create-case modal + search
  useEffect(() => {
    const handleModal = (e) => {
      if (e.detail === "create-case") {
        setShowModal(true);
        setEditCaseId(null);
      }
    };
    const handleSearch = (e) => {
      if (e.detail?.query) {
        const keyword = e.detail.query;
        setSearchKeyword(keyword);
        if (!keyword.trim()) {
          fetchCases();
          return;
        }
        axios.get(`/api/cases/search?keyword=${keyword}`, {
          headers: { Authorization: `Bearer ${token}` }
        }).then(res => setCases(res.data)).catch(() => {});
      }
    };
    window.addEventListener("assistant-open-modal", handleModal);
    window.addEventListener("assistant-search", handleSearch);
    return () => {
      window.removeEventListener("assistant-open-modal", handleModal);
      window.removeEventListener("assistant-search", handleSearch);
    };
  }, []);

  // Global Search navigation — read incoming state
  useEffect(() => {
    if (location.state?.search) {
      const kw = location.state.search;
      setSearchKeyword(kw);
      setHighlightedId(location.state.id || null);
      if (!kw.trim()) { fetchCases(); return; }
      axios.get(`/api/cases/search?keyword=${encodeURIComponent(kw)}`, {
        headers: { Authorization: `Bearer ${token}` }
      }).then(res => setCases(res.data)).catch(() => {});
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  // ---------------- SEARCH CASES ----------------
  const handleSearch = (e) => {
    setSearchKeyword(e.target.value);
  };

  // ---------------- FORM INPUT CHANGE ----------------
  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewCase({ ...newCase, [name]: value });

    if (name === "caseNumber") {
      if (value.length !== 16) {
        setCaseNumberError("Case Number must be exactly 16 digits.");
      } else {
        setCaseNumberError("");
      }
    }
  };

  // ---------------- CREATE/UPDATE CASE ----------------
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!token) {
      setErrorMessage("Please login first.");
      return;
    }
    if (!newCase.clientId) {
      setErrorMessage("Please choose a client for this case.");
      return;
    }
    if (newCase.caseNumber.length !== 16) {
      setCaseNumberError("Case Number must be exactly 16 digits.");
      return;
    }

    const caseToSend = {
      ...newCase,
      amount: newCase.amount ? parseFloat(newCase.amount) : 0,
      client: { id: Number(newCase.clientId) },
    };

    try {
      if (editCaseId) {
        await withLoading(
          axios.put(
            `/api/cases/update/${editCaseId}`,
            caseToSend,
            { headers: { Authorization: `Bearer ${token}` } }
          ),
          "Updating Case..."
        );
        setEditCaseId(null);
      } else {
        await withLoading(
          axios.post(
            "/api/cases/create",
            caseToSend,
            { headers: { Authorization: `Bearer ${token}` } }
          ),
          "Creating Case..."
        );
      }

      setNewCase({
        caseNumber: "",
        caseTitle: "",
        caseType: "",
        courtLevel: "",
        status: "",
        amount: "",
        description: "",
        clientId: "",
      });
      setShowModal(false);
      setCaseNumberError("");
      fetchCases();
      setErrorMessage("");
    } catch (error) {
      console.error("Error saving case:", error);
      if (error.response?.status === 409) {
        setCaseNumberError("Case number already exists.");
        error("Case number already exists.");
      } else {
        setErrorMessage(
          typeof error.response?.data?.message === "string"
            ? error.response.data.message
            : "Failed to save case."
        );
      }
    }
  };

  // ---------------- EDIT CASE ----------------
  const handleEdit = (caseData) => {
    setNewCase({
      caseNumber: caseData.caseNumber || "",
      caseTitle: caseData.caseTitle || "",
      caseType: caseData.caseType || "",
      courtLevel: caseData.courtLevel || "",
      status: caseData.status || "",
      amount: caseData.amount || "",
      description: caseData.description || "",
      clientId: caseData.clientId ? String(caseData.clientId) : "",
    });
    setEditCaseId(caseData.id);
    setShowModal(true);
  };

  // ---------------- DELETE / ARCHIVE CASE ----------------
  const handleDelete = async (id) => {
    if (!token) {
      setErrorMessage("Please login first.");
      return;
    }
    try {
      await withLoading(
        axios.delete(`/api/cases/delete/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        showArchived ? "Deleting Case..." : "Archiving Case..."
      );
      fetchCases();
      setErrorMessage("");
    } catch (error) {
      console.error("Error deleting case:", error);
      const errData = error.response?.data;
      setErrorMessage(typeof errData === "string" ? errData : (errData?.message || "Failed to archive case."));
    }
  };

  const handleRestore = async (id) => {
    if (!token) {
      setErrorMessage("Please login first.");
      return;
    }
    try {
      await withLoading(
        axios.put(`/api/cases/restore/${id}`, {}, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        "Restoring Case..."
      );
      fetchCases();
      setErrorMessage("");
    } catch (error) {
      console.error("Error restoring case:", error);
      const errData = error.response?.data;
      setErrorMessage(typeof errData === "string" ? errData : (errData?.message || "Failed to restore case."));
    }
  };

  // Document functions
  const openCaseDocs = useCallback(async (c) => {
    setDocCase(c);
    setShowCaseDocs(true);
    setCaseDocsLoading(true);
    try {
      const res = await axios.get(`/api/documents/by-case/${c.id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCaseDocs(res.data || []);
    } catch (err) {
      console.error("Error fetching case documents:", err);
      setCaseDocs([]);
    } finally {
      setCaseDocsLoading(false);
    }
  }, [token]);

  const handleDocDownload = async (docId, fileName) => {
    try {
      const res = await axios.get(`/api/documents/download/${docId}`, {
        headers: { Authorization: `Bearer ${token}` }, responseType: "blob"
      });
      const url = URL.createObjectURL(res.data);
      const a = document.createElement("a");
      a.href = url; a.download = fileName;
      document.body.appendChild(a); a.click(); document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) { console.error("Download error:", err); }
  };

  const handleDocPreview = async (docId) => {
    try {
      const res = await axios.get(`/api/documents/preview/${docId}`, {
        headers: { Authorization: `Bearer ${token}` },
        responseType: "blob"
      });
      const url = URL.createObjectURL(res.data);
      window.open(url, "_blank");
    } catch (err) {
      console.error("Preview error:", err);
    }
  };

  const uploadCaseDoc = async () => {
    if (!uploadDocFile || !docCase) return;
    const formData = new FormData();
    formData.append("file", uploadDocFile);
    formData.append("caseId", docCase.id);
    try {
      await withLoading(
        axios.post("/api/documents/upload", formData, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        "Uploading Document..."
      );
      setUploadDocFile(null);
      openCaseDocs(docCase);
    } catch (err) {
      console.error("Upload error:", err);
    }
  };

  return (
    <div className="cases-container">
      <h2>{showArchived ? "Archived Cases" : "My Cases"}</h2>
      {errorMessage && <p className="error-message">{errorMessage}</p>}

      {/* ✅ Top Actions */}
      <div className="cases-top-actions">
        <button
          onClick={() => {
            setShowModal(true);
            setEditCaseId(null);
          }}
        >
          Add New Case
        </button>

        {/* ✅ Search bar */}
        <input
          type="text"
          placeholder="🔍 Search by case number, client name, or email"
          value={searchKeyword}
          onChange={handleSearch}
          className="search-bar"
        />
        <button className="view-archived-btn" onClick={() => setShowArchived(!showArchived)}>
          {showArchived ? "🔙 Back to Active" : "🗄️ View Archived"}
        </button>
      </div>

      {/* ✅ Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{editCaseId ? "Edit Case" : "Add New Case"}</h3>
            <form className="case-form" onSubmit={handleSubmit}>
              <input
                type="text"
                name="caseNumber"
                placeholder="Case Number (16 digits)"
                value={newCase.caseNumber}
                onChange={handleChange}
                required
                className={caseNumberError ? "input-error" : ""}
              />
              {caseNumberError && (
                <p className="field-error">{caseNumberError}</p>
              )}

              <input
                type="text"
                name="caseTitle"
                placeholder="Case Title"
                value={newCase.caseTitle}
                onChange={handleChange}
                required
              />
              <input
                type="text"
                name="caseType"
                placeholder="Case Type"
                value={newCase.caseType}
                onChange={handleChange}
                required
              />

              <select
                name="courtLevel"
                value={newCase.courtLevel}
                onChange={handleChange}
                required
              >
                <option value="">Select Court Level</option>
                <option value="District">District</option>
                <option value="High Court">High Court</option>
                <option value="Supreme Court">Supreme Court</option>
              </select>

              <select
                name="status"
                value={newCase.status}
                onChange={handleChange}
                required
              >
                <option value="">Select Status</option>
                <option value="Active">Active</option>
                <option value="Pending">Pending</option>
                <option value="Closed">Closed</option>
              </select>

              <input
                type="number"
                name="amount"
                placeholder="Amount"
                value={newCase.amount}
                onChange={handleChange}
              />

              {/* ✅ Searchable dropdown for clients */}
              <Select
                options={clients.map((c) => ({
                  value: c.id,
                  label: `${c.name} — ${c.email}`,
                }))}
                value={
                  clients.find((c) => c.id === Number(newCase.clientId))
                    ? {
                        value: Number(newCase.clientId),
                        label: clients.find(
                          (c) => c.id === Number(newCase.clientId)
                        ).name,
                      }
                    : null
                }
                onChange={(selected) =>
                  setNewCase({
                    ...newCase,
                    clientId: selected ? selected.value : "",
                  })
                }
                isClearable
                placeholder="Select Client"
                styles={customSelectStyles}
              />

              <textarea
                name="description"
                placeholder="Description"
                value={newCase.description}
                onChange={handleChange}
              />

              <div style={{ display: "flex", gap: 8 }}>
                <button type="submit">
                  {editCaseId ? "Update Case" : "Save Case"}
                </button>
                <button
                  type="button"
                  className="close-btn"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ✅ Cases Table */}
      <div className="cases-table">
        {pageLoading ? (
          <InlineLoader type="table" rows={size} cols={9} />
        ) : cases.length === 0 ? (
          <p>No cases found.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Case No</th>
                <th>Title</th>
                <th>Type</th>
                <th>Court Level</th>
                <th>Status</th>
                <th>Amount</th>
                <th>Client</th>
                <th>Description</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {cases.map((c) => (
                <tr key={c.id} className={highlightedId === c.id ? "highlight-row" : ""} ref={(el) => { if (highlightedId === c.id && el) el.scrollIntoView({ behavior: "smooth", block: "center" }); }}>
                  <td>{c.caseNumber}</td>
                  <td>{c.caseTitle}</td>
                  <td>{c.caseType}</td>
                  <td>{c.courtLevel}</td>
                  <td>
                    <span className={`status ${c.status.toLowerCase()}`}>
                      {c.status}
                    </span>
                  </td>
                  <td>{formatCurrency(c.amount)}</td>
                  <td>{c.clientName || "N/A"}</td>
                  <td>{c.description}</td>
                    <td className="actions-cell">
                    <div className="action-btns">
                      {showArchived ? (
                        <button className="action-btn restore-btn" onClick={() => handleRestore(c.id)} title="Restore">
                          ♻️ Restore
                        </button>
                      ) : (
                        <>
                          <button className="action-btn edit-btn" onClick={() => handleEdit(c)} title="Edit">
                            <FiEdit2 />
                          </button>
                          <button className="action-btn delete-btn" onClick={() => handleDelete(c.id)} title="Archive">
                            <FiTrash2 />
                          </button>
                        </>
                      )}
                      <button className="action-btn timeline-btn" onClick={() => openTimeline(c)} title="Timeline">
                        <FiClock />
                      </button>
                      <button className="action-btn docs-btn" onClick={() => openCaseDocs(c)} title="Documents">
                        <FiFolder />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <Pagination
          page={page}
          totalPages={totalPages}
          totalElements={totalElements}
          size={size}
          onPageChange={setPage}
          onSizeChange={setSize}
        />
      </div>
      {/* Case Documents Modal */}
      {showCaseDocs && docCase && (
        <div className="modal-overlay" onClick={() => setShowCaseDocs(false)}>
          <div className="modal-content case-docs-modal" onClick={(e) => e.stopPropagation()}>
            <div className="case-docs-header">
              <h3>Documents — {docCase.caseNumber}</h3>
              <button className="close-btn" onClick={() => setShowCaseDocs(false)}><FiX /></button>
            </div>
            {caseDocsLoading ? (
              <p>Loading documents...</p>
            ) : (
              <>
                {caseDocs.length === 0 ? (
                  <p className="no-data">No documents linked to this case.</p>
                ) : (
                  <div className="case-docs-list">
                    {caseDocs.map((d) => (
                      <div key={d.id} className="case-doc-item">
                        <FiFolder size={20} />
                        <span className="case-doc-name">{d.documentName}</span>
                        <span className="case-doc-meta">{d.category || "Other"}</span>
                        <span className="case-doc-meta">{d.version > 1 ? `v${d.version}` : "v1"}</span>
                        <div className="case-doc-actions">
                          <button onClick={() => handleDocPreview(d.id)} title="Preview"><FiEye /></button>
                          <button onClick={() => handleDocDownload(d.id, d.originalName || d.documentName)} title="Download"><FiDownload /></button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
                <div className="case-doc-upload">
                  <input type="file" onChange={(e) => setUploadDocFile(e.target.files[0])} />
                  <button onClick={uploadCaseDoc} disabled={!uploadDocFile}><FiUpload /> Upload</button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
      {/* Case Timeline Modal */}
      {showTimeline && timelineCase && (
        <CaseTimeline
          caseId={timelineCase.id}
          caseNumber={timelineCase.caseNumber}
          onClose={() => { setShowTimeline(false); setTimelineCase(null); }}
        />
      )}
    </div>
  );
}

export default Cases;
