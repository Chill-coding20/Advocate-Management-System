import React, { useState, useEffect, useCallback } from "react";
import { useLocation } from "react-router-dom";
import axios from "axios";
import { FiPlus, FiTrash2, FiCheckSquare, FiSquare, FiSearch } from "react-icons/fi";
import "../assets/styles/TasksPage.css";
import { useLoading } from "../contexts/LoadingContext.jsx";
import Pagination from "../components/Pagination";
import usePagination from "../hooks/usePagination";

export default function TasksPage() {
  const [tasks, setTasks] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [title, setTitle] = useState("");
  const [priority, setPriority] = useState("MEDIUM");
  const [deadline, setDeadline] = useState("");
  const [searchText, setSearchText] = useState("");
  const [highlightedId, setHighlightedId] = useState(null);
  const location = useLocation();
  const { page, setPage, size, setSize } = usePagination({ defaultSize: 20, resetOn: [searchText] });

  const token = localStorage.getItem("token");
  const { withLoading } = useLoading();

  const fetchTasks = useCallback(async () => {
    try {
      const res = await axios.get("/api/tasks", {
        headers: { Authorization: `Bearer ${token}` },
        params: { page, size, keyword: searchText || undefined }
      });
      setTasks(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
    } catch (err) {
      console.error("Error fetching tasks:", err);
    }
  }, [token, page, size, searchText]);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  // Global Search navigation — read incoming state
  useEffect(() => {
    if (location.state?.search) {
      setSearchText(location.state.search);
      setHighlightedId(location.state.id || null);
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const handleCreateTask = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;

    try {
      await withLoading(
        axios.post(
          "/api/tasks/create",
          {
            title,
            priority,
            deadline: deadline || null
          },
          { headers: { Authorization: `Bearer ${token}` } }
        ),
        "Creating Task..."
      );
      setTitle("");
      setPriority("MEDIUM");
      setDeadline("");
      if (page !== 0) setPage(0);
      else fetchTasks();
    } catch (err) {
      console.error("Error creating task:", err);
    }
  };

  const handleToggle = async (id) => {
    try {
      await withLoading(
        axios.put(`/api/tasks/toggle/${id}`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        "Updating Task..."
      );
      fetchTasks();
    } catch (err) {
      console.error("Error toggling task:", err);
    }
  };

  const handleDelete = async (id) => {
    try {
      await withLoading(
        axios.delete(`/api/tasks/delete/${id}`, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        "Deleting Task..."
      );
      fetchTasks();
    } catch (err) {
      console.error("Error deleting task:", err);
    }
  };

  return (
    <div className="tasks-page-container">
      <h2>📋 Personal Todo Tasks</h2>
      <p className="subtle">Manage reminder checklists, task priorities, and schedules.</p>

      {/* Search */}
      <div className="task-search-bar">
        <FiSearch className="task-search-icon" />
        <input
          type="text"
          placeholder="Search tasks..."
          value={searchText}
          onChange={(e) => { setSearchText(e.target.value); setHighlightedId(null); }}
        />
      </div>

      {/* New Task Form */}
      <form onSubmit={handleCreateTask} className="task-creation-form">
        <input
          type="text"
          placeholder="What needs to be done?"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />
        <select value={priority} onChange={(e) => setPriority(e.target.value)}>
          <option value="HIGH">High Priority</option>
          <option value="MEDIUM">Medium Priority</option>
          <option value="LOW">Low Priority</option>
        </select>
        <input
          type="date"
          value={deadline}
          onChange={(e) => setDeadline(e.target.value)}
        />
        <button type="submit">
          <FiPlus /> Add Task
        </button>
      </form>

      {/* Tasks List */}
      <div className="tasks-list-panel">
        {tasks.length === 0 ? (
          <p className="no-data">All caught up! No tasks left.</p>
        ) : (
          <div className="tasks-rows-grid">
            {tasks.map((task) => (
              <div key={task.id} className={`task-row-card ${task.completed ? "completed" : ""}${highlightedId === task.id ? " highlight-row" : ""}`} ref={(el) => { if (highlightedId === task.id && el) el.scrollIntoView({ behavior: "smooth", block: "center" }); }}>
                <button className="toggle-complete-btn" onClick={() => handleToggle(task.id)}>
                  {task.completed ? <FiCheckSquare className="icon-chk checked" /> : <FiSquare className="icon-chk" />}
                </button>
                <div className="task-content">
                  <span className="task-title">{task.title}</span>
                  {task.deadline && (
                    <span className="task-due-date">
                      📅 Due: {new Date(task.deadline).toLocaleDateString()}
                    </span>
                  )}
                </div>
                <div className="task-side-actions">
                  <span className={`priority-tag ${task.priority.toLowerCase()}`}>
                    {task.priority}
                  </span>
                  <button className="delete-task-btn" onClick={() => handleDelete(task.id)}>
                    <FiTrash2 />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <Pagination
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        size={size}
        onPageChange={setPage}
        onSizeChange={setSize}
      />
    </div>
  );
}
