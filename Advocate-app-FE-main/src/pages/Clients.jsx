import React, { useState, useEffect, useCallback, useRef } from "react";
import { useLocation } from "react-router-dom";
import axios from "axios";
import { useLoading } from "../contexts/LoadingContext";
import { FiFolder, FiEye, FiDownload, FiX, FiUpload, FiFile } from "react-icons/fi";
import ReportService from "../services/ReportService";
import Pagination from "../components/Pagination";
import usePagination from "../hooks/usePagination";
import "../assets/styles/Clients.css";

function Clients() {
  const [clients, setClients] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [showArchived, setShowArchived] = useState(false);
  const [newClient, setNewClient] = useState({
    name: "",
    email: "",
    phone: "",
    address: "",
  });
  const [showModal, setShowModal] = useState(false);
  const [editClientId, setEditClientId] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [highlightedId, setHighlightedId] = useState(null);
  const location = useLocation();
  const token = localStorage.getItem("token");
  const { withLoading } = useLoading();
  const { page, setPage, size, setSize } = usePagination({ defaultSize: 20, resetOn: [searchKeyword, showArchived] });
  const searchedFromGlobalNav = useRef(!!location.state?.search);

  // Document tab state
  const [showClientDocs, setShowClientDocs] = useState(false);
  const [docClient, setDocClient] = useState(null);
  const [clientDocs, setClientDocs] = useState([]);
  const [clientDocsLoading, setClientDocsLoading] = useState(false);
  const [uploadClientDocFile, setUploadClientDocFile] = useState(null);

  // ---------------- FETCH CLIENTS ----------------
  const fetchClients = useCallback(async (keyword = "") => {
    try {
      const params = { page, size };
      if (keyword.trim()) params.keyword = keyword;
      if (showArchived) params.archived = true;

      const response = await axios.get("/api/clients", {
        headers: { Authorization: `Bearer ${token}` },
        params,
      });
      setClients(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);
      setErrorMessage("");
    } catch (error) {
      console.error("Error fetching clients:", error);
      const errData = error.response?.data;
      setErrorMessage(typeof errData === "string" ? errData : (errData?.message || "Failed to fetch clients."));
    }
  }, [token, page, size, showArchived]);

  useEffect(() => {
    if (!token) {
      setErrorMessage("Please login first.");
      return;
    }
    if (searchedFromGlobalNav.current) {
      searchedFromGlobalNav.current = false;
      return;
    }
    fetchClients(searchKeyword);
  }, [fetchClients, searchKeyword]);

  // AI Assistant: open create-client modal + search
  useEffect(() => {
    const handleModal = (e) => {
      if (e.detail === "create-client") {
        setNewClient({ name: "", email: "", phone: "", address: "" });
        setEditClientId(null);
        setShowModal(true);
      }
    };
    const handleSearch = (e) => {
      if (e.detail?.query) {
        setSearchKeyword(e.detail.query);
        fetchClients(e.detail.query);
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
      fetchClients(kw);
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const handleSearch = (e) => {
    const keyword = e.target.value;
    setSearchKeyword(keyword);
    fetchClients(keyword);
  };

  const handleChange = (e) => {
    setNewClient({ ...newClient, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editClientId) {
        await withLoading(
          axios.put(
            `/api/clients/update/${editClientId}`,
            newClient,
            { headers: { Authorization: `Bearer ${token}` } }
          ),
          "Updating Client..."
        );
      } else {
        await withLoading(
          axios.post(
            "/api/clients/create",
            newClient,
            { headers: { Authorization: `Bearer ${token}` } }
          ),
          "Saving Client..."
        );
      }
      setNewClient({ name: "", email: "", phone: "", address: "" });
      setShowModal(false);
      fetchClients();
    } catch (error) {
      console.error("Error saving client:", error);
      const errData = error.response?.data;
      setErrorMessage(typeof errData === "string" ? errData : (errData?.message || "Failed to save client."));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Archive this client?")) return;
    try {
      await withLoading(
        axios.delete(`/api/clients/delete/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        "Deleting Client..."
      );
      fetchClients();
    } catch (error) {
      console.error("Error deleting client:", error);
      const errData = error.response?.data;
      setErrorMessage(typeof errData === "string" ? errData : (errData?.message || "Failed to delete client."));
    }
  };

  const handleRestore = async (id) => {
    try {
      await withLoading(
        axios.put(`/api/clients/restore/${id}`, {}, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        "Restoring Client..."
      );
      fetchClients();
    } catch (error) {
      console.error("Error restoring client:", error);
      const errData = error.response?.data;
      setErrorMessage(typeof errData === "string" ? errData : (errData?.message || "Failed to restore client."));
    }
  };

  // Document functions
  const openClientDocs = useCallback(async (c) => {
    setDocClient(c);
    setShowClientDocs(true);
    setClientDocsLoading(true);
    try {
      const res = await axios.get(`/api/documents/by-client/${c.id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setClientDocs(res.data || []);
    } catch (err) {
      console.error("Error fetching client documents:", err);
      setClientDocs([]);
    } finally {
      setClientDocsLoading(false);
    }
  }, [token]);

  const handleClientDocDownload = async (docId, fileName) => {
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

  const handleClientDocPreview = async (docId) => {
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

  const uploadClientDoc = async () => {
    if (!uploadClientDocFile || !docClient) return;
    const formData = new FormData();
    formData.append("file", uploadClientDocFile);
    formData.append("clientId", docClient.id);
    try {
      await withLoading(
        axios.post("/api/documents/upload", formData, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        "Uploading Document..."
      );
      setUploadClientDocFile(null);
      openClientDocs(docClient);
    } catch (err) {
      console.error("Upload error:", err);
    }
  };

  return (
    <div className="clients-container">
      <div className="clients-header">
        <h2>{showArchived ? "Archived Clients" : "My Clients"}</h2>

        <div className="header-actions">
          <input
            type="text"
            placeholder="🔍 Search by name, email, or phone"
            value={searchKeyword}
            onChange={handleSearch}
            className="search-bar"
          />
          <button className="add-client-btn" onClick={() => { setShowModal(true); setEditClientId(null); }}>
            Add New Client
          </button>
          <button className="view-archived-btn" onClick={() => setShowArchived(!showArchived)}>
            {showArchived ? "🔙 Back to Active" : "🗄️ View Archived"}
          </button>
        </div>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{editClientId ? "Edit Client" : "Add New Client"}</h3>
            <form className="client-form" onSubmit={handleSubmit}>
              <input name="name" placeholder="Full Name" value={newClient.name} onChange={handleChange} required />
              <input name="email" type="email" placeholder="Email" value={newClient.email} onChange={handleChange} required />
              <input name="phone" placeholder="Phone Number" value={newClient.phone} onChange={handleChange} required />
              <textarea name="address" placeholder="Address" value={newClient.address} onChange={handleChange} />
              <div className="modal-buttons">
                <button type="submit" className="save-client-btn">{editClientId ? "Update Client" : "Save Client"}</button>
                <button type="button" className="close-btn" onClick={() => setShowModal(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="clients-table">
        {clients.length === 0 ? (
          <p>No clients found.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Name</th><th>Email</th><th>Phone</th><th>Address</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {clients.map((c) => (
                <tr key={c.id} className={highlightedId === c.id ? "highlight-row" : ""} ref={(el) => { if (highlightedId === c.id && el) el.scrollIntoView({ behavior: "smooth", block: "center" }); }}>
                  <td>{c.name}</td><td>{c.email}</td><td>{c.phone}</td><td>{c.address}</td>
                  <td>
                    {showArchived ? (
                      <button className="restore-btn" onClick={() => handleRestore(c.id)}>♻️ Restore</button>
                    ) : (
                      <>
                        <button className="edit-btn" onClick={() => { setNewClient(c); setEditClientId(c.id); setShowModal(true); }}>Edit</button>
                        <button className="archive-btn" onClick={() => handleDelete(c.id)}>Archive</button>
                      </>
                    )}
                    <button className="doc-btn" onClick={() => openClientDocs(c)} title="Documents">
                      <FiFolder />
                    </button>
                    <button className="pdf-btn" onClick={() => ReportService.downloadClientDetail(c.id, c.name)} title="Export PDF">
                      <FiFile />
                    </button>
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

      {/* Client Documents Modal */}
      {showClientDocs && docClient && (
        <div className="modal-overlay" onClick={() => setShowClientDocs(false)}>
          <div className="modal-content case-docs-modal" onClick={(e) => e.stopPropagation()}>
            <div className="case-docs-header">
              <h3>Documents — {docClient.name}</h3>
              <button className="close-btn" onClick={() => setShowClientDocs(false)}><FiX /></button>
            </div>
            {clientDocsLoading ? (
              <p>Loading documents...</p>
            ) : (
              <>
                {clientDocs.length === 0 ? (
                  <p className="no-data">No documents linked to this client.</p>
                ) : (
                  <div className="case-docs-list">
                    {clientDocs.map((d) => (
                      <div key={d.id} className="case-doc-item">
                        <FiFolder size={20} />
                        <span className="case-doc-name">{d.documentName}</span>
                        <span className="case-doc-meta">{d.category || "Other"}</span>
                        <span className="case-doc-meta">{d.version > 1 ? `v${d.version}` : "v1"}</span>
                        <div className="case-doc-actions">
                          <button onClick={() => handleClientDocPreview(d.id)} title="Preview"><FiEye /></button>
                          <button onClick={() => handleClientDocDownload(d.id, d.originalName || d.documentName)} title="Download"><FiDownload /></button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
                <div className="case-doc-upload">
                  <input type="file" onChange={(e) => setUploadClientDocFile(e.target.files[0])} />
                  <button onClick={uploadClientDoc} disabled={!uploadClientDocFile}><FiUpload /> Upload</button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default Clients;
